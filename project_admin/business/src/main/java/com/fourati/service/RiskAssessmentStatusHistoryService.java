package com.fourati.service;

import com.fourati.dto.response.RiskAssessmentStatusHistoryResponse;
import com.fourati.mapper.RiskAssessmentStatusHistoryMapper;
import com.fourati.repository.RiskAssessmentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link RiskAssessmentService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskAssessmentStatusHistoryService {

    private final RiskAssessmentStatusHistoryRepository riskAssessmentStatusHistoryRepository;
    private final RiskAssessmentStatusHistoryMapper riskAssessmentStatusHistoryMapper;

    public List<RiskAssessmentStatusHistoryResponse> findByRiskAssessmentId(UUID riskAssessmentId) {
        return riskAssessmentStatusHistoryRepository.findByRiskAssessmentIdOrderByChangedAtAsc(riskAssessmentId).stream()
                .map(riskAssessmentStatusHistoryMapper::toResponse)
                .toList();
    }
}
