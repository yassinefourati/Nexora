package com.fourati.service;

import com.fourati.dto.response.ContractSignatureStatusHistoryResponse;
import com.fourati.mapper.ContractSignatureStatusHistoryMapper;
import com.fourati.repository.ContractSignatureStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link ContractSignatureService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractSignatureStatusHistoryService {

    private final ContractSignatureStatusHistoryRepository contractSignatureStatusHistoryRepository;
    private final ContractSignatureStatusHistoryMapper contractSignatureStatusHistoryMapper;

    public List<ContractSignatureStatusHistoryResponse> findByContractSignatureId(UUID contractSignatureId) {
        return contractSignatureStatusHistoryRepository.findByContractSignatureIdOrderByChangedAtAsc(contractSignatureId).stream()
                .map(contractSignatureStatusHistoryMapper::toResponse)
                .toList();
    }
}
