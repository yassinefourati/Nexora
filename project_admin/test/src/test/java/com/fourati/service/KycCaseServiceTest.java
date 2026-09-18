package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.KycCase;
import com.fourati.dto.request.CompleteKycCaseRequest;
import com.fourati.dto.request.CreateKycCaseRequest;
import com.fourati.dto.response.KycCaseResponse;
import com.fourati.mapper.KycCaseMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.KycCaseRepository;
import com.fourati.repository.KycStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycCaseServiceTest {

    @Mock
    private KycCaseRepository kycCaseRepository;

    @Mock
    private KycStatusHistoryRepository kycStatusHistoryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private KycCaseMapper kycCaseMapper;

    @InjectMocks
    private KycCaseService kycCaseService;

    @Test
    void create_opensCaseAndRecordsInitialStatus() {
        UUID customerId = UUID.randomUUID();
        CreateKycCaseRequest request = new CreateKycCaseRequest(customerId);
        KycCase entity = new KycCase();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(new Customer()));
        when(kycCaseMapper.toEntity(request)).thenReturn(entity);
        when(kycCaseRepository.save(any(KycCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(kycCaseMapper.toResponse(any(KycCase.class))).thenReturn(
                new KycCaseResponse(UUID.randomUUID(), customerId, "pending", null, null, null, null));

        KycCaseResponse response = kycCaseService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        verify(kycStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsNotFound_whenCustomerMissing() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kycCaseService.create(new CreateKycCaseRequest(customerId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void startReview_transitionsPendingToInProgress() {
        UUID id = UUID.randomUUID();
        KycCase entity = new KycCase();
        entity.setStatus("pending");
        when(kycCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(kycCaseRepository.save(any(KycCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(kycCaseMapper.toResponse(any(KycCase.class))).thenReturn(
                new KycCaseResponse(id, null, "in_progress", null, null, null, null));

        KycCaseResponse response = kycCaseService.startReview(id);

        assertThat(response.status()).isEqualTo("in_progress");
        verify(kycStatusHistoryRepository).save(any());
    }

    @Test
    void complete_rejectsInvalidOutcome() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> kycCaseService.complete(id, new CompleteKycCaseRequest("bogus", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void complete_setsOutcomeAndCompletedAt() {
        UUID id = UUID.randomUUID();
        KycCase entity = new KycCase();
        entity.setStatus("in_progress");
        when(kycCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(kycCaseRepository.save(any(KycCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(kycCaseMapper.toResponse(any(KycCase.class))).thenReturn(
                new KycCaseResponse(id, null, "passed", null, null, null, null));

        KycCaseResponse response = kycCaseService.complete(id, new CompleteKycCaseRequest("passed", "all checks clear"));

        assertThat(response.status()).isEqualTo("passed");
        assertThat(entity.getCompletedAt()).isNotNull();
        verify(kycStatusHistoryRepository).save(any());
    }
}
