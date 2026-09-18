package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.RiskAssessment;
import com.fourati.dto.request.CreateRiskAssessmentRequest;
import com.fourati.dto.response.RiskAssessmentResponse;
import com.fourati.mapper.RiskAssessmentMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.RiskAssessmentFactorRepository;
import com.fourati.repository.RiskAssessmentRepository;
import com.fourati.repository.RiskAssessmentStatusHistoryRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private RiskAssessmentFactorRepository riskAssessmentFactorRepository;

    @Mock
    private RiskAssessmentStatusHistoryRepository riskAssessmentStatusHistoryRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private RiskAssessmentMapper riskAssessmentMapper;

    @InjectMocks
    private RiskAssessmentService riskAssessmentService;

    @Test
    void create_throwsConflict_whenApplicationAlreadyHasAssessment() {
        UUID loanApplicationId = UUID.randomUUID();
        when(riskAssessmentRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(true);

        assertThatThrownBy(() -> riskAssessmentService.create(new CreateRiskAssessmentRequest(loanApplicationId)))
                .isInstanceOf(ConflictException.class);

        verify(riskAssessmentRepository, never()).save(any());
    }

    @Test
    void create_opensAssessmentInPendingStatus() {
        UUID loanApplicationId = UUID.randomUUID();
        when(riskAssessmentRepository.existsByLoanApplicationId(loanApplicationId)).thenReturn(false);
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(new LoanApplication()));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(riskAssessmentMapper.toResponse(any(RiskAssessment.class))).thenReturn(
                new RiskAssessmentResponse(UUID.randomUUID(), loanApplicationId, "pending", null, null, null, null, null));

        RiskAssessmentResponse response = riskAssessmentService.create(new CreateRiskAssessmentRequest(loanApplicationId));

        assertThat(response.status()).isEqualTo("pending");
        verify(riskAssessmentStatusHistoryRepository).save(any());
    }

    @Test
    void process_throwsConflict_whenNotPending() {
        UUID id = UUID.randomUUID();
        RiskAssessment entity = new RiskAssessment();
        entity.setStatus("completed");
        when(riskAssessmentRepository.findById(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> riskAssessmentService.process(id))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void process_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(riskAssessmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> riskAssessmentService.process(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void process_scoresAssessesFactorsAndCompletes() {
        UUID id = UUID.randomUUID();
        RiskAssessment entity = new RiskAssessment();
        entity.setStatus("pending");
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        entity.setLoanApplication(application);

        when(riskAssessmentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(riskAssessmentMapper.toResponse(any(RiskAssessment.class))).thenReturn(
                new RiskAssessmentResponse(id, null, "completed", 50, "medium", null, null, null));

        RiskAssessmentResponse response = riskAssessmentService.process(id);

        assertThat(response.status()).isEqualTo("completed");
        assertThat(entity.getRiskScore()).isNotNull();
        assertThat(entity.getRiskClass()).isNotNull();
        assertThat(entity.getAssessedAt()).isNotNull();
        verify(riskAssessmentFactorRepository, times(6)).save(any());
    }
}
