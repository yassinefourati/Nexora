package com.fourati.service;

import com.fourati.domain.Tag;
import com.fourati.dto.request.CreateTagRequest;
import com.fourati.dto.request.UpdateTagRequest;
import com.fourati.dto.response.TagResponse;
import com.fourati.mapper.TagMapper;
import com.fourati.repository.TagRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Audited(action = "CREATE", description = "Create a new tag")
    public TagResponse create(CreateTagRequest request) {
        if (tagRepository.existsByName(request.name())) {
            throw new ConflictException("Tag already exists with name: " + request.name());
        }
        Tag entity = tagMapper.toEntity(request);
        Tag saved = tagRepository.save(entity);
        return tagMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TagResponse findById(UUID id) {
        return tagMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TagResponse> findAll(Pageable pageable) {
        return tagRepository.findAll(pageable).map(tagMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a tag")
    public TagResponse update(UUID id, UpdateTagRequest request) {
        Tag entity = getEntityOrThrow(id);
        if (!entity.getName().equals(request.name()) && tagRepository.existsByName(request.name())) {
            throw new ConflictException("Tag already exists with name: " + request.name());
        }
        tagMapper.updateEntityFromRequest(request, entity);
        Tag saved = tagRepository.save(entity);
        return tagMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a tag")
    public void delete(UUID id) {
        Tag entity = getEntityOrThrow(id);
        entity.setDeletedAt(java.time.Instant.now());
        tagRepository.save(entity);
    }

    private Tag getEntityOrThrow(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }
}
