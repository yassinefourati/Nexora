package com.fourati.service;

import com.fourati.domain.FraudAlert;
import com.fourati.domain.FraudCheck;
import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CreateFraudCheckRequest;
import com.fourati.dto.response.FraudCheckResponse;
import com.fourati.mapper.FraudCheckMapper;
import com.fourati.repository.FraudAlertRepository;
import com.fourati.repository.FraudCheckRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Fraud detection rules are never exposed through the API — only outcomes
 * (score, status, alerts). Scoring here is a deterministic placeholder
 * (derived from the loan application id) standing in for a real fraud-rules
 * engine; the point of this milestone is the check lifecycle and alerting,
 * not a production detection model.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FraudCheckService {

    private static final int FLAG_THRESHOLD = 70;

    private final FraudCheckRepository fraudCheckRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final FraudCheckMapper fraudCheckMapper;

    @Audited(action = "CREATE", description = "Open a fraud check for a loan application")
    public FraudCheckResponse create(CreateFraudCheckRequest request) {
        if (fraudCheckRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("Loan application " + request.loanApplicationId() + " already has a fraud check");
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));

        FraudCheck entity = new FraudCheck();
        entity.setLoanApplication(loanApplication);
        FraudCheck saved = fraudCheckRepository.save(entity);
        return fraudCheckMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FraudCheckResponse findById(UUID id) {
        return fraudCheckMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FraudCheckResponse> findAll(Pageable pageable) {
        return fraudCheckRepository.findAll(pageable).map(fraudCheckMapper::toResponse);
    }

    @Audited(action = "PROCESS", description = "Score a fraud check and raise an alert if flagged")
    public FraudCheckResponse process(UUID id) {
        FraudCheck entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Fraud check " + id + " must be pending to process, was: " + entity.getStatus());
        }
        entity.setStatus("in_progress");
        fraudCheckRepository.save(entity);

        int fraudScore = Math.floorMod(entity.getLoanApplication().getId().hashCode(), 100);
        entity.setFraudScore(fraudScore);
        entity.setCheckedAt(Instant.now());

        if (fraudScore >= FLAG_THRESHOLD) {
            entity.setStatus("flagged");
            FraudAlert alert = new FraudAlert();
            alert.setFraudCheck(entity);
            alert.setIndicatorType("suspicious_behavior");
            alert.setSeverity(fraudScore >= 90 ? "critical" : "high");
            fraudAlertRepository.save(alert);
        } else {
            entity.setStatus("clear");
        }

        FraudCheck saved = fraudCheckRepository.save(entity);
        return fraudCheckMapper.toResponse(saved);
    }

    private FraudCheck getEntityOrThrow(UUID id) {
        return fraudCheckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FraudCheck", id));
    }
}
