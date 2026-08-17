package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CreditCheck;
import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CreateCreditCheckRequest;
import com.fourati.dto.response.CreditCheckResponse;
import com.fourati.integration.credit.CreditBureauClient;
import com.fourati.integration.credit.CreditBureauReportResponse;
import com.fourati.mapper.CreditCheckMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CreditAssessmentRepository;
import com.fourati.repository.CreditCheckRepository;
import com.fourati.repository.CreditCheckStatusHistoryRepository;
import com.fourati.repository.CreditReportRepository;
import com.fourati.repository.CreditScoreRepository;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.LoanApplicationRepository;
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
class CreditCheckServiceTest {

    @Mock
    private CreditCheckRepository creditCheckRepository;

    @Mock
    private CreditReportRepository creditReportRepository;

    @Mock
    private CreditScoreRepository creditScoreRepository;

    @Mock
    private CreditAssessmentRepository creditAssessmentRepository;

    @Mock
    private CreditCheckStatusHistoryRepository creditCheckStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CreditCheckMapper creditCheckMapper;

    @Mock
    private CreditBureauClient creditBureauClient;

    @InjectMocks
    private CreditCheckService creditCheckService;

    @Test
    void create_throwsConflict_whenApplicationAlreadyHasCreditCheck() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(creditCheckRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(true);

        assertThatThrownBy(() -> creditCheckService.create(new CreateCreditCheckRequest(loanApplicationId, customerId)))
                .isInstanceOf(ConflictException.class);

        verify(creditCheckRepository, never()).save(any());
    }

    @Test
    void create_opensCheckInPendingStatus() {
        UUID loanApplicationId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(creditCheckRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));
        when(creditCheckRepository.save(any(CreditCheck.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creditCheckMapper.toResponse(any(CreditCheck.class))).thenReturn(
                new CreditCheckResponse(UUID.randomUUID(), loanApplicationId, customerId, "pending", null, null, null, null));

        CreditCheckResponse response = creditCheckService.create(new CreateCreditCheckRequest(loanApplicationId, customerId));

        assertThat(response.status()).isEqualTo("pending");
        verify(creditCheckStatusHistoryRepository).save(any());
    }

    @Test
    void process_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(creditCheckRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditCheckService.process(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void process_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        CreditCheck entity = new CreditCheck();
        entity.setStatus("completed");
        when(creditCheckRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> creditCheckService.process(id))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void process_createsReportScoreAssessmentAndCompletesCheck() {
        UUID id = UUID.randomUUID();
        CreditCheck entity = new CreditCheck();
        entity.setStatus("pending");
        entity.setCustomer(new Customer());

        when(creditCheckRepository.findById(id)).thenReturn(Optional.of(entity));
        when(creditCheckRepository.save(any(CreditCheck.class))).thenAnswer(inv -> inv.getArgument(0));
        when(creditBureauClient.getCreditReport(any())).thenReturn(
                new CreditBureauReportResponse("MockBureau", "REF-1", 700, 700, "FICO-MOCK", BigDecimal.valueOf(0.3)));
        when(creditCheckMapper.toResponse(any(CreditCheck.class))).thenReturn(
                new CreditCheckResponse(id, null, null, "completed", null, null, null, null));

        CreditCheckResponse response = creditCheckService.process(id);

        assertThat(response.status()).isEqualTo("completed");
        assertThat(entity.getCompletedAt()).isNotNull();
        verify(creditReportRepository).save(any());
        verify(creditScoreRepository).save(any());
        verify(creditAssessmentRepository).save(any());
    }
}
