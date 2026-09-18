package com.fourati.service;

import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanInstallment;
import com.fourati.domain.LoanRepayment;
import com.fourati.domain.LoanRepaymentStatusHistory;
import com.fourati.dto.request.CompleteLoanRepaymentRequest;
import com.fourati.dto.request.CreateLoanRepaymentRequest;
import com.fourati.dto.request.FailLoanRepaymentRequest;
import com.fourati.dto.response.LoanRepaymentResponse;
import com.fourati.mapper.LoanRepaymentMapper;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanInstallmentRepository;
import com.fourati.repository.LoanRepaymentRepository;
import com.fourati.repository.LoanRepaymentStatusHistoryRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repayments are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanRepaymentStatusHistory} so the
 * trail survives a future migration to Camunda-driven orchestration. A
 * repayment can only be recorded against an installment that belongs to
 * the given account and is not already paid. On completion the
 * installment is marked paid and the account's outstanding principal is
 * reduced by the installment's principal share.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanRepaymentService {

    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanRepaymentStatusHistoryRepository loanRepaymentStatusHistoryRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanRepaymentMapper loanRepaymentMapper;

    @Audited(action = "CREATE", description = "Record a new loan repayment")
    public LoanRepaymentResponse create(CreateLoanRepaymentRequest request) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.loanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanAccount", request.loanAccountId()));
        LoanInstallment loanInstallment = loanInstallmentRepository.findById(request.loanInstallmentId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanInstallment", request.loanInstallmentId()));
        if (!loanInstallment.getLoanAccount().getId().equals(loanAccount.getId())) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId()
                    + " does not belong to loan account " + request.loanAccountId());
        }
        if ("paid".equals(loanInstallment.getStatus())) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId() + " is already paid");
        }
        if (loanRepaymentRepository.existsByLoanInstallmentIdAndStatus(request.loanInstallmentId(), "pending")) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId() + " already has a pending repayment");
        }

        LoanRepayment entity = loanRepaymentMapper.toEntity(request);
        entity.setLoanAccount(loanAccount);
        entity.setLoanInstallment(loanInstallment);
        LoanRepayment saved = loanRepaymentRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Repayment recorded");
        return loanRepaymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanRepaymentResponse findById(UUID id) {
        return loanRepaymentMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanRepaymentResponse> findAll(Pageable pageable) {
        return loanRepaymentRepository.findAll(pageable).map(loanRepaymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> findByLoanAccountId(UUID loanAccountId) {
        return loanRepaymentRepository.findByLoanAccountId(loanAccountId).stream()
                .map(loanRepaymentMapper::toResponse)
                .toList();
    }

    @Audited(action = "COMPLETE", description = "Complete a loan repayment")
    public LoanRepaymentResponse complete(UUID id, CompleteLoanRepaymentRequest request) {
        LoanRepayment entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan repayment " + id + " must be pending to complete, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setReferenceNumber(request.referenceNumber());
        entity.setPaidAt(Instant.now());
        entity.setStatus("completed");
        LoanRepayment saved = loanRepaymentRepository.save(entity);
        recordStatusChange(saved, previousStatus, "completed", "Repayment completed, reference: " + request.referenceNumber());

        LoanInstallment installment = entity.getLoanInstallment();
        installment.setStatus("paid");
        loanInstallmentRepository.save(installment);

        LoanAccount account = entity.getLoanAccount();
        account.setOutstandingPrincipal(account.getOutstandingPrincipal().subtract(installment.getPrincipalAmount()));
        loanAccountRepository.save(account);

        return loanRepaymentMapper.toResponse(saved);
    }

    @Audited(action = "FAIL", description = "Mark a loan repayment as failed")
    public LoanRepaymentResponse fail(UUID id, FailLoanRepaymentRequest request) {
        LoanRepayment entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan repayment " + id + " must be pending to fail, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setFailureReason(request.failureReason());
        entity.setFailedAt(Instant.now());
        entity.setStatus("failed");
        LoanRepayment saved = loanRepaymentRepository.save(entity);
        recordStatusChange(saved, previousStatus, "failed", request.failureReason());
        return loanRepaymentMapper.toResponse(saved);
    }

    private void recordStatusChange(LoanRepayment loanRepayment, String fromStatus, String toStatus, String reason) {
        LoanRepaymentStatusHistory history = new LoanRepaymentStatusHistory();
        history.setLoanRepayment(loanRepayment);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanRepaymentStatusHistoryRepository.save(history);
    }

    private LoanRepayment getEntityOrThrow(UUID id) {
        return loanRepaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanRepayment", id));
    }
}
