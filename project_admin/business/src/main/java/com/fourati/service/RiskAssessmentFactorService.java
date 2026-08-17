package com.fourati.service;

import com.fourati.dto.response.RiskAssessmentFactorResponse;
import com.fourati.mapper.RiskAssessmentFactorMapper;
import com.fourati.repository.RiskAssessmentFactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link RiskAssessmentService#process}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskAssessmentFactorService {

    private final RiskAssessmentFactorRepository riskAssessmentFactorRepository;
    private final RiskAssessmentFactorMapper riskAssessmentFactorMapper;

    public List<RiskAssessmentFactorResponse> findByRiskAssessmentId(UUID riskAssessmentId) {
        return riskAssessmentFactorRepository.findByRiskAssessmentId(riskAssessmentId).stream()
                .map(riskAssessmentFactorMapper::toResponse)
                .toList();
    }
}
