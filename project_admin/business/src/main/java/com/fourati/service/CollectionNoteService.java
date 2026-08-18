package com.fourati.service;

import com.fourati.domain.CollectionCase;
import com.fourati.domain.CollectionNote;
import com.fourati.dto.request.CreateCollectionNoteRequest;
import com.fourati.dto.response.CollectionNoteResponse;
import com.fourati.mapper.CollectionNoteMapper;
import com.fourati.repository.CollectionCaseRepository;
import com.fourati.repository.CollectionNoteRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CollectionNoteService {

    private final CollectionNoteRepository collectionNoteRepository;
    private final CollectionCaseRepository collectionCaseRepository;
    private final CollectionNoteMapper collectionNoteMapper;

    @Audited(action = "CREATE", description = "Add a note to a collection case")
    public CollectionNoteResponse create(CreateCollectionNoteRequest request) {
        CollectionCase collectionCase = collectionCaseRepository.findById(request.collectionCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("CollectionCase", request.collectionCaseId()));

        CollectionNote entity = collectionNoteMapper.toEntity(request);
        entity.setCollectionCase(collectionCase);
        CollectionNote saved = collectionNoteRepository.save(entity);
        return collectionNoteMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CollectionNoteResponse> findByCollectionCaseId(UUID collectionCaseId) {
        return collectionNoteRepository.findByCollectionCaseIdOrderByCreatedAtAsc(collectionCaseId).stream()
                .map(collectionNoteMapper::toResponse)
                .toList();
    }
}
