package com.fourati.service;

import com.fourati.dto.response.DocumentReviewResponse;
import com.fourati.mapper.DocumentReviewMapper;
import com.fourati.repository.DocumentReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link DocumentService#review}
 * on every review decision, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentReviewService {

    private final DocumentReviewRepository documentReviewRepository;
    private final DocumentReviewMapper documentReviewMapper;

    public List<DocumentReviewResponse> findByDocumentId(UUID documentId) {
        return documentReviewRepository.findByDocumentIdOrderByReviewedAtAsc(documentId).stream()
                .map(documentReviewMapper::toResponse)
                .toList();
    }
}
