package com.fourati.service;

import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanAccountStatusHistory;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanDisbursement;
import com.fourati.domain.LoanInstallment;
import com.fourati.dto.request.CloseLoanAccountRequest;
import com.fourati.dto.request.CreateLoanAccountRequest;
import com.fourati.dto.request.DefaultLoanAccountRequest;
import com.fourati.dto.response.LoanAccountResponse;
import com.fourati.mapper.LoanAccountMapper;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanAccountStatusHistoryRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanDisbursementRepository;
import com.fourati.repository.LoanInstallmentRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Loan accounts are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanAccountStatusHistory} so the trail
 * survives a future migration to Camunda-driven orchestration. An account
 * can only be opened once against a completed {@link LoanDisbursement}, at
 * which point a reducing-balance {@link LoanInstallment} schedule (equal
 * principal per installment, interest on the declining balance) is
 * generated for the account's term. Payment capture against installments
 * belongs to a later Repayment module.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanAccountService {

    private static final int MONEY_SCALE = 2;
    private static final int RATE_SCALE = 10;

    private final LoanAccountRepository loanAccountRepository;
    private final LoanAccountStatusHistoryRepository loanAccountStatusHistoryRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanAccountMapper loanAccountMapper;

    @Audited(action = "CREATE", description = "Open a new loan account and generate its repayment schedule")
    public LoanAccountResponse create(CreateLoanAccountRequest request) {
        if (loanAccountRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("A loan account already exists for loan application " + request.loanApplicationId());
        }
        if (loanAccountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new ConflictException("Account number " + request.accountNumber() + " is already in use");
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanDisbursement loanDisbursement = loanDisbursementRepository.findById(request.loanDisbursementId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanDisbursement", request.loanDisbursementId()));
        if (!"completed".equals(loanDisbursement.getStatus())) {
            throw new ConflictException("Loan disbursement " + request.loanDisbursementId()
                    + " must be completed before a loan account can be opened");
        }

        LoanAccount entity = loanAccountMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setLoanDisbursement(loanDisbursement);
        entity.setPrincipalAmount(loanDisbursement.getAmount());
        entity.setInterestRate(loanDisbursement.getLoanContract().getInterestRate());
        entity.setTermMonths(loanDisbursement.getLoanContract().getTermMonths());
        entity.setOutstandingPrincipal(loanDisbursement.getAmount());
        LoanAccount saved = loanAccountRepository.save(entity);
        recordStatusChange(saved, null, "active", "Loan account opened");
        generateInstallmentSchedule(saved);
        return loanAccountMapper.toResponse(saved);
    }

    private void generateInstallmentSchedule(LoanAccount account) {
        BigDecimal monthlyRate = account.getInterestRate()
                .divide(BigDecimal.valueOf(100), RATE_SCALE, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal principalPerInstallment = account.getPrincipalAmount()
                .divide(BigDecimal.valueOf(account.getTermMonths()), MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = account.getPrincipalAmount();
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int installmentNumber = 1; installmentNumber <= account.getTermMonths(); installmentNumber++) {
            boolean isLastInstallment = installmentNumber == account.getTermMonths();
            BigDecimal principal = isLastInstallment ? remainingBalance : principalPerInstallment;
            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);

            LoanInstallment installment = new LoanInstallment();
            installment.setLoanAccount(account);
            installment.setInstallmentNumber(installmentNumber);
            installment.setDueDate(dueDate.plusMonths(installmentNumber - 1L));
            installment.setPrincipalAmount(principal);
            installment.setInterestAmount(interest);
            installment.setTotalAmount(principal.add(interest));
            loanInstallmentRepository.save(installment);

            remainingBalance = remainingBalance.subtract(principal);
        }
    }

    @Transactional(readOnly = true)
    public LoanAccountResponse findById(UUID id) {
        return loanAccountMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanAccountResponse> findAll(Pageable pageable) {
        return loanAccountRepository.findAll(pageable).map(loanAccountMapper::toResponse);
    }

    @Audited(action = "CLOSE", description = "Close a loan account")
    public LoanAccountResponse close(UUID id, CloseLoanAccountRequest request) {
        LoanAccount entity = getEntityOrThrow(id);
        if (!"active".equals(entity.getStatus())) {
            throw new ConflictException("Loan account " + id + " must be active to close, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("closed");
        entity.setClosedAt(Instant.now());
        LoanAccount saved = loanAccountRepository.save(entity);
        recordStatusChange(saved, previousStatus, "closed", request.reason());
        return loanAccountMapper.toResponse(saved);
    }

    @Audited(action = "DEFAULT", description = "Mark a loan account as defaulted")
    public LoanAccountResponse markDefaulted(UUID id, DefaultLoanAccountRequest request) {
        LoanAccount entity = getEntityOrThrow(id);
        if (!"active".equals(entity.getStatus())) {
            throw new ConflictException("Loan account " + id + " must be active to default, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("defaulted");
        entity.setClosedAt(Instant.now());
        LoanAccount saved = loanAccountRepository.save(entity);
        recordStatusChange(saved, previousStatus, "defaulted", request.reason());
        return loanAccountMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan account")
    public void delete(UUID id) {
        LoanAccount entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanAccountRepository.save(entity);
    }

    private void recordStatusChange(LoanAccount loanAccount, String fromStatus, String toStatus, String reason) {
        LoanAccountStatusHistory history = new LoanAccountStatusHistory();
        history.setLoanAccount(loanAccount);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanAccountStatusHistoryRepository.save(history);
    }

    private LoanAccount getEntityOrThrow(UUID id) {
        return loanAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanAccount", id));
    }
}
