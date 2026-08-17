package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.RiskAssessment;
import com.fourati.domain.RiskAssessmentFactor;
import com.fourati.domain.RiskAssessmentStatusHistory;
import com.fourati.dto.request.CreateRiskAssessmentRequest;
import com.fourati.dto.response.RiskAssessmentResponse;
import com.fourati.mapper.RiskAssessmentMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.RiskAssessmentFactorRepository;
import com.fourati.repository.RiskAssessmentRepository;
import com.fourati.repository.RiskAssessmentStatusHistoryRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Status transitions are explicit-state (no workflow engine yet, same
 * approach as CreditCheck/KycCase); every transition is recorded in
 * {@link RiskAssessmentStatusHistory}. Scoring here is a deterministic
 * placeholder (derived from the loan application id) standing in for a
 * real risk-rules engine — the point of this milestone is the assessment
 * lifecycle and audit trail, not a production scoring model.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RiskAssessmentService {

    private static final List<String> FACTOR_TYPES = List.of(
            "credit_score", "debt_to_income_ratio", "income_stability",
            "employment_duration", "loan_to_income_ratio", "existing_debt");

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskAssessmentFactorRepository riskAssessmentFactorRepository;
    private final RiskAssessmentStatusHistoryRepository riskAssessmentStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RiskAssessmentMapper riskAssessmentMapper;

    @Audited(action = "CREATE", description = "Open a risk assessment for a loan application")
    public RiskAssessmentResponse create(CreateRiskAssessmentRequest request) {
        if (riskAssessmentRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("Loan application " + request.loanApplicationId() + " already has a risk assessment");
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));

        RiskAssessment entity = new RiskAssessment();
        entity.setLoanApplication(loanApplication);
        RiskAssessment saved = riskAssessmentRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Risk assessment opened");
        return riskAssessmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RiskAssessmentResponse findById(UUID id) {
        return riskAssessmentMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<RiskAssessmentResponse> findAll(Pageable pageable) {
        return riskAssessmentRepository.findAll(pageable).map(riskAssessmentMapper::toResponse);
    }

    @Audited(action = "PROCESS", description = "Score and classify a risk assessment")
    public RiskAssessmentResponse process(UUID id) {
        RiskAssessment entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Risk assessment " + id + " must be pending to process, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("in_progress");
        riskAssessmentRepository.save(entity);
        recordStatusChange(entity, previousStatus, "in_progress", "Scoring started");

        int seed = Math.floorMod(entity.getLoanApplication().getId().hashCode(), 100);
        for (String factorType : FACTOR_TYPES) {
            RiskAssessmentFactor factor = new RiskAssessmentFactor();
            factor.setRiskAssessment(entity);
            factor.setFactorType(factorType);
            factor.setFactorValue(BigDecimal.valueOf(seed));
            factor.setWeight(BigDecimal.valueOf(1.0 / FACTOR_TYPES.size()).setScale(4, java.math.RoundingMode.HALF_UP));
            riskAssessmentFactorRepository.save(factor);
        }

        int riskScore = 100 - seed;
        entity.setRiskScore(riskScore);
        entity.setRiskClass(riskClassFor(riskScore));
        entity.setStatus("completed");
        entity.setAssessedAt(Instant.now());
        RiskAssessment saved = riskAssessmentRepository.save(entity);
        recordStatusChange(saved, "in_progress", "completed", "Risk class: " + saved.getRiskClass());

        return riskAssessmentMapper.toResponse(saved);
    }

    private String riskClassFor(int riskScore) {
        if (riskScore <= 25) {
            return "low";
        }
        if (riskScore <= 50) {
            return "medium";
        }
        if (riskScore <= 75) {
            return "high";
        }
        return "very_high";
    }

    private void recordStatusChange(RiskAssessment riskAssessment, String fromStatus, String toStatus, String reason) {
        RiskAssessmentStatusHistory history = new RiskAssessmentStatusHistory();
        history.setRiskAssessment(riskAssessment);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        riskAssessmentStatusHistoryRepository.save(history);
    }

    private RiskAssessment getEntityOrThrow(UUID id) {
        return riskAssessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RiskAssessment", id));
    }
}
