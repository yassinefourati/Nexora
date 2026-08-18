package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApproval;
import com.fourati.domain.LoanApprovalStatusHistory;
import com.fourati.domain.UnderwritingCase;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.RejectLoanApprovalRequest;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.mapper.LoanApprovalMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.repository.LoanApprovalStatusHistoryRepository;
import com.fourati.repository.UnderwritingCaseRepository;
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
 * Approvals are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanApprovalStatusHistory} so the trail
 * survives a future migration to Camunda-driven orchestration. A record can
 * only be opened against a completed {@link UnderwritingCase} — the
 * underwriting decision it formalizes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanApprovalService {

    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanApprovalStatusHistoryRepository loanApprovalStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UnderwritingCaseRepository underwritingCaseRepository;
    private final LoanApprovalMapper loanApprovalMapper;

    @Audited(action = "CREATE", description = "Open a new loan approval record")
    public LoanApprovalResponse create(CreateLoanApprovalRequest request) {
        if (loanApprovalRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("A loan approval already exists for loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        UnderwritingCase underwritingCase = underwritingCaseRepository.findById(request.underwritingCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("UnderwritingCase", request.underwritingCaseId()));
        if (!"completed".equals(underwritingCase.getStatus())) {
            throw new ConflictException("Underwriting case " + request.underwritingCaseId()
                    + " must be completed before a loan approval can be opened");
        }

        LoanApproval entity = loanApprovalMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setUnderwritingCase(underwritingCase);
        LoanApproval saved = loanApprovalRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Loan approval opened");
        return loanApprovalMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanApprovalResponse findById(UUID id) {
        return loanApprovalMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanApprovalResponse> findAll(Pageable pageable) {
        return loanApprovalRepository.findAll(pageable).map(loanApprovalMapper::toResponse);
    }

    @Audited(action = "APPROVE", description = "Approve a loan and record its final terms")
    public LoanApprovalResponse approve(UUID id, ApproveLoanApprovalRequest request) {
        LoanApproval entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan approval " + id + " must be pending to approve, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setApprovedAmount(request.approvedAmount());
        entity.setApprovedTermMonths(request.approvedTermMonths());
        entity.setInterestRate(request.interestRate());
        entity.setApprovedBy(request.approvedBy());
        entity.setApprovedAt(Instant.now());
        entity.setStatus("approved");
        LoanApproval saved = loanApprovalRepository.save(entity);
        recordStatusChange(saved, previousStatus, "approved", "Loan approved by " + request.approvedBy());
        return loanApprovalMapper.toResponse(saved);
    }

    @Audited(action = "REJECT", description = "Reject a loan approval")
    public LoanApprovalResponse reject(UUID id, RejectLoanApprovalRequest request) {
        LoanApproval entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan approval " + id + " must be pending to reject, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setRejectionReason(request.rejectionReason());
        entity.setStatus("rejected");
        LoanApproval saved = loanApprovalRepository.save(entity);
        recordStatusChange(saved, previousStatus, "rejected", request.rejectionReason());
        return loanApprovalMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan approval")
    public void delete(UUID id) {
        LoanApproval entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanApprovalRepository.save(entity);
    }

    private void recordStatusChange(LoanApproval loanApproval, String fromStatus, String toStatus, String reason) {
        LoanApprovalStatusHistory history = new LoanApprovalStatusHistory();
        history.setLoanApproval(loanApproval);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanApprovalStatusHistoryRepository.save(history);
    }

    private LoanApproval getEntityOrThrow(UUID id) {
        return loanApprovalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApproval", id));
    }
}
