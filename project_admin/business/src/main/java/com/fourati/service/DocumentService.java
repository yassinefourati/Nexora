package com.fourati.service;

import com.fourati.domain.Document;
import com.fourati.domain.DocumentReview;
import com.fourati.dto.request.CreateDocumentRequest;
import com.fourati.dto.request.ReviewDocumentRequest;
import com.fourati.dto.response.DocumentResponse;
import com.fourati.mapper.DocumentMapper;
import com.fourati.repository.DocumentRepository;
import com.fourati.repository.DocumentReviewRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentReviewRepository documentReviewRepository;
    private final DocumentMapper documentMapper;

    @Audited(action = "CREATE", description = "Register a new uploaded document")
    public DocumentResponse create(CreateDocumentRequest request) {
        if (documentRepository.existsByStorageKey(request.storageKey())) {
            throw new ConflictException("Document already exists with storage key: " + request.storageKey());
        }
        Document entity = documentMapper.toEntity(request);
        Document saved = documentRepository.save(entity);
        return documentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(UUID id) {
        return documentMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> findAll(Pageable pageable) {
        return documentRepository.findAll(pageable).map(documentMapper::toResponse);
    }

    @Audited(action = "REVIEW", description = "Review a document")
    public DocumentResponse review(UUID id, ReviewDocumentRequest request) {
        Document entity = getEntityOrThrow(id);
        if (!"uploaded".equals(entity.getStatus()) && !"under_review".equals(entity.getStatus())) {
            throw new ConflictException("Document " + id + " cannot be reviewed from status: " + entity.getStatus());
        }
        entity.setStatus(request.decision());
        Document saved = documentRepository.save(entity);

        DocumentReview review = new DocumentReview();
        review.setDocument(saved);
        review.setDecision(request.decision());
        review.setComments(request.comments());
        documentReviewRepository.save(review);

        return documentMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a document")
    public void delete(UUID id) {
        Document entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        documentRepository.save(entity);
    }

    private Document getEntityOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", id));
    }
}
