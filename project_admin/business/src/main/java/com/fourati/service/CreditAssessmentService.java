package com.fourati.service;

import com.fourati.dto.response.CreditAssessmentResponse;
import com.fourati.mapper.CreditAssessmentMapper;
import com.fourati.repository.CreditAssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link CreditCheckService#process}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditAssessmentService {

    private final CreditAssessmentRepository creditAssessmentRepository;
    private final CreditAssessmentMapper creditAssessmentMapper;

    public List<CreditAssessmentResponse> findByCreditCheckId(UUID creditCheckId) {
        return creditAssessmentRepository.findByCreditCheckId(creditCheckId).stream()
                .map(creditAssessmentMapper::toResponse)
                .toList();
    }
}
