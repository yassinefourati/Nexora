package com.fourati.service;

import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanContract;
import com.fourati.domain.LoanDisbursement;
import com.fourati.dto.request.CloseLoanAccountRequest;
import com.fourati.dto.request.CreateLoanAccountRequest;
import com.fourati.dto.request.DefaultLoanAccountRequest;
import com.fourati.dto.response.LoanAccountResponse;
import com.fourati.mapper.LoanAccountMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanAccountStatusHistoryRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanDisbursementRepository;
import com.fourati.repository.LoanInstallmentRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanAccountServiceTest {

    @Mock
    private LoanAccountRepository loanAccountRepository;

    @Mock
    private LoanAccountStatusHistoryRepository loanAccountStatusHistoryRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;

    @Mock
    private LoanAccountMapper loanAccountMapper;

    @InjectMocks
    private LoanAccountService loanAccountService;

    private LoanDisbursement completedDisbursement() {
        LoanContract contract = new LoanContract();
        contract.setInterestRate(BigDecimal.valueOf(6));
        contract.setTermMonths(12);

        LoanDisbursement disbursement = new LoanDisbursement();
        disbursement.setStatus("completed");
        disbursement.setAmount(BigDecimal.valueOf(12000));
        disbursement.setLoanContract(contract);
        return disbursement;
    }

    @Test
    void create_generatesFullInstallmentScheduleAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanDisbursementId = UUID.randomUUID();
        CreateLoanAccountRequest request = new CreateLoanAccountRequest(loanApplicationId, loanDisbursementId, "ACC-0001");
        LoanDisbursement disbursement = completedDisbursement();
        LoanAccount entity = new LoanAccount();

        when(loanAccountRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanAccountRepository.existsByAccountNumber("ACC-0001")).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanDisbursementRepository.findById(loanDisbursementId)).thenReturn(Optional.of(disbursement));
        when(loanAccountMapper.toEntity(request)).thenReturn(entity);
        when(loanAccountRepository.save(any(LoanAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanInstallmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountMapper.toResponse(any(LoanAccount.class))).thenReturn(
                new LoanAccountResponse(UUID.randomUUID(), loanApplicationId, loanDisbursementId, "ACC-0001", "active",
                        BigDecimal.valueOf(12000), BigDecimal.valueOf(6), 12, BigDecimal.valueOf(12000), null, null, null, null));

        LoanAccountResponse response = loanAccountService.create(request);

        assertThat(response.status()).isEqualTo("active");
        assertThat(entity.getPrincipalAmount()).isEqualByComparingTo(BigDecimal.valueOf(12000));
        assertThat(entity.getTermMonths()).isEqualTo(12);
        verify(loanInstallmentRepository, times(12)).save(any());
        verify(loanAccountStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenDisbursementNotCompleted() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID loanDisbursementId = UUID.randomUUID();
        LoanDisbursement disbursement = completedDisbursement();
        disbursement.setStatus("initiated");

        when(loanAccountRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanAccountRepository.existsByAccountNumber("ACC-0002")).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(loanDisbursementRepository.findById(loanDisbursementId)).thenReturn(Optional.of(disbursement));

        assertThatThrownBy(() -> loanAccountService.create(
                new CreateLoanAccountRequest(loanApplicationId, loanDisbursementId, "ACC-0002")))
                .isInstanceOf(ConflictException.class);

        verify(loanAccountRepository, never()).save(any());
        verify(loanInstallmentRepository, never()).save(any());
    }

    @Test
    void close_transitionsActiveToClosedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanAccount entity = new LoanAccount();
        entity.setStatus("active");

        when(loanAccountRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanAccountRepository.save(any(LoanAccount.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanAccountMapper.toResponse(any(LoanAccount.class))).thenReturn(
                new LoanAccountResponse(id, null, null, "ACC-0001", "closed", null, null, 0, null, null, null, null, null));

        LoanAccountResponse response = loanAccountService.close(id, new CloseLoanAccountRequest("paid off"));

        assertThat(response.status()).isEqualTo("closed");
        assertThat(entity.getClosedAt()).isNotNull();
        verify(loanAccountStatusHistoryRepository).save(any());
    }

    @Test
    void markDefaulted_throwsConflict_whenNotActive() {
        UUID id = UUID.randomUUID();
        LoanAccount entity = new LoanAccount();
        entity.setStatus("closed");
        when(loanAccountRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanAccountService.markDefaulted(id, new DefaultLoanAccountRequest("missed payments")))
                .isInstanceOf(ConflictException.class);

        verify(loanAccountRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(loanAccountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanAccountService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
