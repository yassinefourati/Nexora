package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApproval;
import com.fourati.domain.LoanOffer;
import com.fourati.domain.LoanOfferStatusHistory;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.DeclineLoanOfferRequest;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.mapper.LoanOfferMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.repository.LoanOfferRepository;
import com.fourati.repository.LoanOfferStatusHistoryRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Offers are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanOfferStatusHistory} so the trail
 * survives a future migration to Camunda-driven orchestration. A record can
 * only be issued against an approved {@link LoanApproval}, whose final
 * terms are copied onto the offer at issuance time.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanOfferService {

    private final LoanOfferRepository loanOfferRepository;
    private final LoanOfferStatusHistoryRepository loanOfferStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanOfferMapper loanOfferMapper;

    @Audited(action = "CREATE", description = "Issue a new loan offer")
    public LoanOfferResponse create(CreateLoanOfferRequest request) {
        if (loanOfferRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("A loan offer already exists for loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanApproval loanApproval = loanApprovalRepository.findById(request.loanApprovalId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApproval", request.loanApprovalId()));
        if (!"approved".equals(loanApproval.getStatus())) {
            throw new ConflictException("Loan approval " + request.loanApprovalId()
                    + " must be approved before a loan offer can be issued");
        }

        LoanOffer entity = loanOfferMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setLoanApproval(loanApproval);
        entity.setOfferedAmount(loanApproval.getApprovedAmount());
        entity.setOfferedTermMonths(loanApproval.getApprovedTermMonths());
        entity.setInterestRate(loanApproval.getInterestRate());
        LoanOffer saved = loanOfferRepository.save(entity);
        recordStatusChange(saved, null, "issued", "Loan offer issued");
        return loanOfferMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanOfferResponse findById(UUID id) {
        return loanOfferMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanOfferResponse> findAll(Pageable pageable) {
        return loanOfferRepository.findAll(pageable).map(loanOfferMapper::toResponse);
    }

    @Audited(action = "ACCEPT", description = "Accept a loan offer")
    public LoanOfferResponse accept(UUID id) {
        LoanOffer entity = getEntityOrThrow(id);
        if (!"issued".equals(entity.getStatus())) {
            throw new ConflictException("Loan offer " + id + " must be issued to accept, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("accepted");
        entity.setAcceptedAt(Instant.now());
        LoanOffer saved = loanOfferRepository.save(entity);
        recordStatusChange(saved, previousStatus, "accepted", "Offer accepted by customer");
        return loanOfferMapper.toResponse(saved);
    }

    @Audited(action = "DECLINE", description = "Decline a loan offer")
    public LoanOfferResponse decline(UUID id, DeclineLoanOfferRequest request) {
        LoanOffer entity = getEntityOrThrow(id);
        if (!"issued".equals(entity.getStatus())) {
            throw new ConflictException("Loan offer " + id + " must be issued to decline, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("declined");
        entity.setDeclineReason(request.declineReason());
        entity.setDeclinedAt(Instant.now());
        LoanOffer saved = loanOfferRepository.save(entity);
        recordStatusChange(saved, previousStatus, "declined", request.declineReason());
        return loanOfferMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan offer")
    public void delete(UUID id) {
        LoanOffer entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanOfferRepository.save(entity);
    }

    private void recordStatusChange(LoanOffer loanOffer, String fromStatus, String toStatus, String reason) {
        LoanOfferStatusHistory history = new LoanOfferStatusHistory();
        history.setLoanOffer(loanOffer);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanOfferStatusHistoryRepository.save(history);
    }

    private LoanOffer getEntityOrThrow(UUID id) {
        return loanOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanOffer", id));
    }
}
