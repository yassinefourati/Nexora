package com.fourati.service;

import com.fourati.dto.response.CreditScoreResponse;
import com.fourati.mapper.CreditScoreMapper;
import com.fourati.repository.CreditScoreRepository;
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
public class CreditScoreService {

    private final CreditScoreRepository creditScoreRepository;
    private final CreditScoreMapper creditScoreMapper;

    public List<CreditScoreResponse> findByCreditCheckId(UUID creditCheckId) {
        return creditScoreRepository.findByCreditCheckId(creditCheckId).stream()
                .map(creditScoreMapper::toResponse)
                .toList();
    }
}
