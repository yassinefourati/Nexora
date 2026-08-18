package com.fourati.service;

import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanInstallment;
import com.fourati.domain.LoanRepayment;
import com.fourati.dto.request.CompleteLoanRepaymentRequest;
import com.fourati.dto.request.CreateLoanRepaymentRequest;
import com.fourati.dto.request.FailLoanRepaymentRequest;
import com.fourati.dto.response.LoanRepaymentResponse;
import com.fourati.mapper.LoanRepaymentMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanInstallmentRepository;
import com.fourati.repository.LoanRepaymentRepository;
import com.fourati.repository.LoanRepaymentStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanRepaymentServiceTest {

    @Mock
    private LoanRepaymentRepository loanRepaymentRepository;

    @Mock
    private LoanRepaymentStatusHistoryRepository loanRepaymentStatusHistoryRepository;

    @Mock
    private LoanAccountRepository loanAccountRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private LoanRepaymentMapper loanRepaymentMapper;

    @InjectMocks
    private LoanRepaymentService loanRepaymentService;

    private LoanAccount accountWithId(UUID id) {
        LoanAccount account = new LoanAccount();
        account.setId(id);
        account.setOutstandingPrincipal(BigDecimal.valueOf(12000));
        return account;
    }

    @Test
    void create_savesRepaymentInPendingAndRecordsStatusHistory() {
        UUID loanAccountId = UUID.randomUUID();
        UUID loanInstallmentId = UUID.randomUUID();
        CreateLoanRepaymentRequest request = new CreateLoanRepaymentRequest(
                loanAccountId, loanInstallmentId, BigDecimal.valueOf(1050), "bank_transfer");

        LoanAccount account = accountWithId(loanAccountId);
        LoanInstallment installment = new LoanInstallment();
        installment.setLoanAccount(account);
        installment.setStatus("pending");
        installment.setPrincipalAmount(BigDecimal.valueOf(1000));
        LoanRepayment entity = new LoanRepayment();

        when(loanAccountRepository.findById(loanAccountId)).thenReturn(Optional.of(account));
        when(loanInstallmentRepository.findById(loanInstallmentId)).thenReturn(Optional.of(installment));
        when(loanRepaymentRepository.existsByLoanInstallmentIdAndStatus(loanInstallmentId, "pending")).thenReturn(false);
        when(loanRepaymentMapper.toEntity(request)).thenReturn(entity);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepaymentMapper.toResponse(any(LoanRepayment.class))).thenReturn(
                new LoanRepaymentResponse(UUID.randomUUID(), loanAccountId, loanInstallmentId, "pending",
                        BigDecimal.valueOf(1050), "bank_transfer", null, null, null, null, null, null));

        LoanRepaymentResponse response = loanRepaymentService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        verify(loanRepaymentStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenInstallmentAlreadyPaid() {
        UUID loanAccountId = UUID.randomUUID();
        UUID loanInstallmentId = UUID.randomUUID();
        LoanAccount account = accountWithId(loanAccountId);
        LoanInstallment installment = new LoanInstallment();
        installment.setLoanAccount(account);
        installment.setStatus("paid");

        when(loanAccountRepository.findById(loanAccountId)).thenReturn(Optional.of(account));
        when(loanInstallmentRepository.findById(loanInstallmentId)).thenReturn(Optional.of(installment));

        assertThatThrownBy(() -> loanRepaymentService.create(
                new CreateLoanRepaymentRequest(loanAccountId, loanInstallmentId, BigDecimal.valueOf(1050), "bank_transfer")))
                .isInstanceOf(ConflictException.class);

        verify(loanRepaymentRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenInstallmentBelongsToDifferentAccount() {
        UUID loanAccountId = UUID.randomUUID();
        UUID loanInstallmentId = UUID.randomUUID();
        LoanAccount account = accountWithId(loanAccountId);
        LoanInstallment installment = new LoanInstallment();
        installment.setLoanAccount(accountWithId(UUID.randomUUID()));
        installment.setStatus("pending");

        when(loanAccountRepository.findById(loanAccountId)).thenReturn(Optional.of(account));
        when(loanInstallmentRepository.findById(loanInstallmentId)).thenReturn(Optional.of(installment));

        assertThatThrownBy(() -> loanRepaymentService.create(
                new CreateLoanRepaymentRequest(loanAccountId, loanInstallmentId, BigDecimal.valueOf(1050), "bank_transfer")))
                .isInstanceOf(ConflictException.class);

        verify(loanRepaymentRepository, never()).save(any());
    }

    @Test
    void complete_marksInstallmentPaidAndReducesOutstandingPrincipal() {
        UUID id = UUID.randomUUID();
        LoanAccount account = accountWithId(UUID.randomUUID());
        LoanInstallment installment = new LoanInstallment();
        installment.setStatus("pending");
        installment.setPrincipalAmount(BigDecimal.valueOf(1000));

        LoanRepayment entity = new LoanRepayment();
        entity.setStatus("pending");
        entity.setLoanAccount(account);
        entity.setLoanInstallment(installment);

        when(loanRepaymentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanRepaymentMapper.toResponse(any(LoanRepayment.class))).thenReturn(
                new LoanRepaymentResponse(id, null, null, "completed", BigDecimal.valueOf(1050), "bank_transfer",
                        "REF-1", null, null, null, null, null));

        LoanRepaymentResponse response = loanRepaymentService.complete(id, new CompleteLoanRepaymentRequest("REF-1"));

        assertThat(response.status()).isEqualTo("completed");
        assertThat(installment.getStatus()).isEqualTo("paid");
        assertThat(account.getOutstandingPrincipal()).isEqualByComparingTo(BigDecimal.valueOf(11000));
        verify(loanInstallmentRepository).save(installment);
        verify(loanAccountRepository).save(account);
        verify(loanRepaymentStatusHistoryRepository).save(any());
    }

    @Test
    void fail_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        LoanRepayment entity = new LoanRepayment();
        entity.setStatus("completed");
        when(loanRepaymentRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanRepaymentService.fail(id, new FailLoanRepaymentRequest("card declined")))
                .isInstanceOf(ConflictException.class);

        verify(loanRepaymentRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanRepaymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanRepaymentService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
