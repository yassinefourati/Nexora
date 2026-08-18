package com.fourati.service;

import com.fourati.dto.response.CollectionCaseStatusHistoryResponse;
import com.fourati.mapper.CollectionCaseStatusHistoryMapper;
import com.fourati.repository.CollectionCaseStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link CollectionCaseService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionCaseStatusHistoryService {

    private final CollectionCaseStatusHistoryRepository collectionCaseStatusHistoryRepository;
    private final CollectionCaseStatusHistoryMapper collectionCaseStatusHistoryMapper;

    public List<CollectionCaseStatusHistoryResponse> findByCollectionCaseId(UUID collectionCaseId) {
        return collectionCaseStatusHistoryRepository.findByCollectionCaseIdOrderByChangedAtAsc(collectionCaseId).stream()
                .map(collectionCaseStatusHistoryMapper::toResponse)
                .toList();
    }
}
