package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CancelLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.mapper.LoanApplicationMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApplicationStatusHistoryRepository;
import com.fourati.repository.LoanProductRepository;
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
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationStatusHistoryRepository loanApplicationStatusHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private LoanApplicationMapper loanApplicationMapper;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private CreateLoanApplicationRequest newRequest(UUID customerId, UUID loanProductId) {
        return new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(10000), 24, "Home renovation");
    }

    @Test
    void create_savesApplicationInDraftAndRecordsStatusHistory() {
        UUID customerId = UUID.randomUUID();
        UUID loanProductId = UUID.randomUUID();
        CreateLoanApplicationRequest request = newRequest(customerId, loanProductId);
        LoanApplication entity = new LoanApplication();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));
        when(loanProductRepository.findById(loanProductId)).thenReturn(Optional.of(new LoanProduct()));
        when(loanApplicationMapper.toEntity(request)).thenReturn(entity);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanApplicationMapper.toResponse(any(LoanApplication.class))).thenReturn(
                new LoanApplicationResponse(UUID.randomUUID(), customerId, loanProductId, "draft",
                        request.requestedAmount(), request.requestedTermMonths(), request.purpose(), null, null, null));

        LoanApplicationResponse response = loanApplicationService.create(request);

        assertThat(response.status()).isEqualTo("draft");
        verify(loanApplicationStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsNotFound_whenCustomerMissing() {
        UUID customerId = UUID.randomUUID();
        UUID loanProductId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.create(newRequest(customerId, loanProductId)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    void submit_transitionsDraftToSubmittedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        LoanApplication entity = new LoanApplication();
        entity.setStatus("draft");
        when(loanApplicationRepository.findById(id)).thenReturn(Optional.of(entity));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));
        when(loanApplicationMapper.toResponse(any(LoanApplication.class))).thenReturn(
                new LoanApplicationResponse(id, null, null, "submitted", null, 0, null, null, null, null));

        LoanApplicationResponse response = loanApplicationService.submit(id);

        assertThat(response.status()).isEqualTo("submitted");
        assertThat(entity.getSubmittedAt()).isNotNull();
        verify(loanApplicationStatusHistoryRepository).save(any());
    }

    @Test
    void submit_throwsConflict_whenNotDraft() {
        UUID id = UUID.randomUUID();
        LoanApplication entity = new LoanApplication();
        entity.setStatus("submitted");
        when(loanApplicationRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanApplicationService.submit(id))
                .isInstanceOf(ConflictException.class);

        verify(loanApplicationRepository, never()).save(any());
    }

    @Test
    void cancel_throwsConflict_whenAlreadyApproved() {
        UUID id = UUID.randomUUID();
        LoanApplication entity = new LoanApplication();
        entity.setStatus("approved");
        when(loanApplicationRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> loanApplicationService.cancel(id, new CancelLoanApplicationRequest("changed mind")))
                .isInstanceOf(ConflictException.class);

        verify(loanApplicationRepository, never()).save(any());
    }
}
