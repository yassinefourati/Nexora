package com.fourati.service;

import com.fourati.dto.response.UnderwritingStatusHistoryResponse;
import com.fourati.mapper.UnderwritingStatusHistoryMapper;
import com.fourati.repository.UnderwritingStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link UnderwritingCaseService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnderwritingStatusHistoryService {

    private final UnderwritingStatusHistoryRepository underwritingStatusHistoryRepository;
    private final UnderwritingStatusHistoryMapper underwritingStatusHistoryMapper;

    public List<UnderwritingStatusHistoryResponse> findByUnderwritingCaseId(UUID underwritingCaseId) {
        return underwritingStatusHistoryRepository.findByUnderwritingCaseIdOrderByChangedAtAsc(underwritingCaseId).stream()
                .map(underwritingStatusHistoryMapper::toResponse)
                .toList();
    }
}
