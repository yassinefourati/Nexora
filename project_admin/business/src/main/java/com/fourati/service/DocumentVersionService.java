package com.fourati.service;

import com.fourati.domain.Document;
import com.fourati.domain.DocumentVersion;
import com.fourati.dto.request.CreateDocumentVersionRequest;
import com.fourati.dto.response.DocumentVersionResponse;
import com.fourati.mapper.DocumentVersionMapper;
import com.fourati.repository.DocumentRepository;
import com.fourati.repository.DocumentVersionRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentVersionService {

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final DocumentVersionMapper documentVersionMapper;

    @Audited(action = "CREATE", description = "Add a version to a document")
    public DocumentVersionResponse create(CreateDocumentVersionRequest request) {
        if (documentVersionRepository.existsByDocumentIdAndVersionNumber(request.documentId(), request.versionNumber())) {
            throw new ConflictException("Document " + request.documentId()
                    + " already has version " + request.versionNumber());
        }
        Document document = documentRepository.findById(request.documentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", request.documentId()));
        DocumentVersion entity = documentVersionMapper.toEntity(request);
        entity.setDocument(document);
        DocumentVersion saved = documentVersionRepository.save(entity);
        return documentVersionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> findByDocumentId(UUID documentId) {
        return documentVersionRepository.findByDocumentId(documentId).stream()
                .map(documentVersionMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a version from a document")
    public void delete(UUID id) {
        DocumentVersion entity = documentVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentVersion", id));
        documentVersionRepository.delete(entity);
    }
}
