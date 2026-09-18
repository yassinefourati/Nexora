package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanContract;
import com.fourati.domain.LoanContractStatusHistory;
import com.fourati.domain.LoanOffer;
import com.fourati.dto.request.CancelLoanContractRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.mapper.LoanContractMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanContractRepository;
import com.fourati.repository.LoanContractStatusHistoryRepository;
import com.fourati.repository.LoanOfferRepository;
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
 * Contracts are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanContractStatusHistory} so the trail
 * survives a future migration to Camunda-driven orchestration. A contract
 * can only be generated against an accepted {@link LoanOffer}, whose final
 * terms are copied onto the contract at generation time. Signature capture
 * belongs to a later Signature module — this service only tracks the
 * contract document's own draft/finalized/cancelled lifecycle.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanContractService {

    private final LoanContractRepository loanContractRepository;
    private final LoanContractStatusHistoryRepository loanContractStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final LoanContractMapper loanContractMapper;

    @Audited(action = "CREATE", description = "Generate a new loan contract")
    public LoanContractResponse create(CreateLoanContractRequest request) {
        if (loanContractRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("A loan contract already exists for loan application " + request.loanApplicationId());
        }
        if (loanContractRepository.existsByContractNumber(request.contractNumber())) {
            throw new ConflictException("Contract number " + request.contractNumber() + " is already in use");
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanOffer loanOffer = loanOfferRepository.findById(request.loanOfferId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanOffer", request.loanOfferId()));
        if (!"accepted".equals(loanOffer.getStatus())) {
            throw new ConflictException("Loan offer " + request.loanOfferId()
                    + " must be accepted before a loan contract can be generated");
        }

        LoanContract entity = loanContractMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setLoanOffer(loanOffer);
        entity.setPrincipalAmount(loanOffer.getOfferedAmount());
        entity.setTermMonths(loanOffer.getOfferedTermMonths());
        entity.setInterestRate(loanOffer.getInterestRate());
        LoanContract saved = loanContractRepository.save(entity);
        recordStatusChange(saved, null, "draft", "Contract generated");
        return loanContractMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanContractResponse findById(UUID id) {
        return loanContractMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanContractResponse> findAll(Pageable pageable) {
        return loanContractRepository.findAll(pageable).map(loanContractMapper::toResponse);
    }

    @Audited(action = "FINALIZE", description = "Finalize a loan contract")
    public LoanContractResponse finalizeContract(UUID id, FinalizeLoanContractRequest request) {
        LoanContract entity = getEntityOrThrow(id);
        if (!"draft".equals(entity.getStatus())) {
            throw new ConflictException("Loan contract " + id + " must be draft to finalize, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setDocumentUrl(request.documentUrl());
        entity.setFinalizedAt(Instant.now());
        entity.setStatus("finalized");
        LoanContract saved = loanContractRepository.save(entity);
        recordStatusChange(saved, previousStatus, "finalized", "Contract finalized");
        return loanContractMapper.toResponse(saved);
    }

    @Audited(action = "CANCEL", description = "Cancel a loan contract")
    public LoanContractResponse cancel(UUID id, CancelLoanContractRequest request) {
        LoanContract entity = getEntityOrThrow(id);
        if ("cancelled".equals(entity.getStatus())) {
            throw new ConflictException("Loan contract " + id + " is already cancelled");
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("cancelled");
        entity.setCancellationReason(request.cancellationReason());
        entity.setCancelledAt(Instant.now());
        LoanContract saved = loanContractRepository.save(entity);
        recordStatusChange(saved, previousStatus, "cancelled", request.cancellationReason());
        return loanContractMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan contract")
    public void delete(UUID id) {
        LoanContract entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanContractRepository.save(entity);
    }

    private void recordStatusChange(LoanContract loanContract, String fromStatus, String toStatus, String reason) {
        LoanContractStatusHistory history = new LoanContractStatusHistory();
        history.setLoanContract(loanContract);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanContractStatusHistoryRepository.save(history);
    }

    private LoanContract getEntityOrThrow(UUID id) {
        return loanContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanContract", id));
    }
}
