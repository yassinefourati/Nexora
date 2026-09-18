package com.fourati.service;

import com.fourati.dto.response.CreditCheckStatusHistoryResponse;
import com.fourati.mapper.CreditCheckStatusHistoryMapper;
import com.fourati.repository.CreditCheckStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link CreditCheckService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditCheckStatusHistoryService {

    private final CreditCheckStatusHistoryRepository creditCheckStatusHistoryRepository;
    private final CreditCheckStatusHistoryMapper creditCheckStatusHistoryMapper;

    public List<CreditCheckStatusHistoryResponse> findByCreditCheckId(UUID creditCheckId) {
        return creditCheckStatusHistoryRepository.findByCreditCheckIdOrderByChangedAtAsc(creditCheckId).stream()
                .map(creditCheckStatusHistoryMapper::toResponse)
                .toList();
    }
}
