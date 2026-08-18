package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.UnderwritingCase;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.mapper.UnderwritingCaseMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.UnderwritingCaseRepository;
import com.fourati.repository.UnderwritingStatusHistoryRepository;
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
class UnderwritingCaseServiceTest {

    @Mock
    private UnderwritingCaseRepository underwritingCaseRepository;

    @Mock
    private UnderwritingStatusHistoryRepository underwritingStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UnderwritingCaseMapper underwritingCaseMapper;

    @InjectMocks
    private UnderwritingCaseService underwritingCaseService;

    @Test
    void create_savesCaseInPendingAndRecordsStatusHistory() {
        UUID loanApplicationId = UUID.randomUUID();
        CreateUnderwritingCaseRequest request = new CreateUnderwritingCaseRequest(loanApplicationId, "jane.underwriter");
        UnderwritingCase entity = new UnderwritingCase();

        when(underwritingCaseRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(underwritingCaseMapper.toEntity(request)).thenReturn(entity);
        when(underwritingCaseRepository.save(any(UnderwritingCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(underwritingCaseMapper.toResponse(any(UnderwritingCase.class))).thenReturn(
                new UnderwritingCaseResponse(UUID.randomUUID(), loanApplicationId, "pending", null, null, null, null,
                        "jane.underwriter", null, null, null));

        UnderwritingCaseResponse response = underwritingCaseService.create(request);

        assertThat(response.status()).isEqualTo("pending");
        verify(underwritingStatusHistoryRepository).save(any());
    }

    @Test
    void create_throwsConflict_whenCaseAlreadyExistsForApplication() {
        UUID loanApplicationId = UUID.randomUUID();
        when(underwritingCaseRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(true);

        assertThatThrownBy(() -> underwritingCaseService.create(new CreateUnderwritingCaseRequest(loanApplicationId, null)))
                .isInstanceOf(ConflictException.class);

        verify(underwritingCaseRepository, never()).save(any());
    }

    @Test
    void startReview_transitionsPendingToInReviewAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        UnderwritingCase entity = new UnderwritingCase();
        entity.setStatus("pending");
        when(underwritingCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(underwritingCaseRepository.save(any(UnderwritingCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(underwritingCaseMapper.toResponse(any(UnderwritingCase.class))).thenReturn(
                new UnderwritingCaseResponse(id, null, "in_review", null, null, null, null, null, null, null, null));

        UnderwritingCaseResponse response = underwritingCaseService.startReview(id);

        assertThat(response.status()).isEqualTo("in_review");
        verify(underwritingStatusHistoryRepository).save(any());
    }

    @Test
    void startReview_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        UnderwritingCase entity = new UnderwritingCase();
        entity.setStatus("in_review");
        when(underwritingCaseRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> underwritingCaseService.startReview(id))
                .isInstanceOf(ConflictException.class);

        verify(underwritingCaseRepository, never()).save(any());
    }

    @Test
    void decide_transitionsInReviewToCompletedAndRecordsHistory() {
        UUID id = UUID.randomUUID();
        UnderwritingCase entity = new UnderwritingCase();
        entity.setStatus("in_review");
        DecideUnderwritingCaseRequest request = new DecideUnderwritingCaseRequest(
                "approve", "Strong credit profile", BigDecimal.valueOf(15000), 36);

        when(underwritingCaseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(underwritingCaseRepository.save(any(UnderwritingCase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(underwritingCaseMapper.toResponse(any(UnderwritingCase.class))).thenReturn(
                new UnderwritingCaseResponse(id, null, "completed", "approve", "Strong credit profile",
                        BigDecimal.valueOf(15000), 36, null, null, null, null));

        UnderwritingCaseResponse response = underwritingCaseService.decide(id, request);

        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.decision()).isEqualTo("approve");
        assertThat(entity.getDecidedAt()).isNotNull();
        verify(underwritingStatusHistoryRepository).save(any());
    }

    @Test
    void decide_throwsConflict_whenNotInReview() {
        UUID id = UUID.randomUUID();
        UnderwritingCase entity = new UnderwritingCase();
        entity.setStatus("pending");
        when(underwritingCaseRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> underwritingCaseService.decide(id,
                new DecideUnderwritingCaseRequest("reject", "Insufficient income", null, null)))
                .isInstanceOf(ConflictException.class);

        verify(underwritingCaseRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(underwritingCaseRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underwritingCaseService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
