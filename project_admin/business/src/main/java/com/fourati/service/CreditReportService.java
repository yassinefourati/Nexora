package com.fourati.service;

import com.fourati.dto.response.CreditReportResponse;
import com.fourati.mapper.CreditReportMapper;
import com.fourati.repository.CreditReportRepository;
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
public class CreditReportService {

    private final CreditReportRepository creditReportRepository;
    private final CreditReportMapper creditReportMapper;

    public List<CreditReportResponse> findByCreditCheckId(UUID creditCheckId) {
        return creditReportRepository.findByCreditCheckId(creditCheckId).stream()
                .map(creditReportMapper::toResponse)
                .toList();
    }
}
