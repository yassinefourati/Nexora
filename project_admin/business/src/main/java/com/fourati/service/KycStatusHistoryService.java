package com.fourati.service;

import com.fourati.dto.response.KycStatusHistoryResponse;
import com.fourati.mapper.KycStatusHistoryMapper;
import com.fourati.repository.KycStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link KycCaseService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KycStatusHistoryService {

    private final KycStatusHistoryRepository kycStatusHistoryRepository;
    private final KycStatusHistoryMapper kycStatusHistoryMapper;

    public List<KycStatusHistoryResponse> findByKycCaseId(UUID kycCaseId) {
        return kycStatusHistoryRepository.findByKycCaseIdOrderByChangedAtAsc(kycCaseId).stream()
                .map(kycStatusHistoryMapper::toResponse)
                .toList();
    }
}
