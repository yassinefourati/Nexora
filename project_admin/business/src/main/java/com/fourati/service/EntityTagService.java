package com.fourati.service;

import com.fourati.domain.EntityTag;
import com.fourati.domain.Tag;
import com.fourati.dto.request.CreateEntityTagRequest;
import com.fourati.dto.response.EntityTagResponse;
import com.fourati.mapper.EntityTagMapper;
import com.fourati.repository.EntityTagRepository;
import com.fourati.repository.TagRepository;
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
public class EntityTagService {

    private final EntityTagRepository entityTagRepository;
    private final TagRepository tagRepository;
    private final EntityTagMapper entityTagMapper;

    @Audited(action = "CREATE", description = "Attach a tag to an entity")
    public EntityTagResponse create(CreateEntityTagRequest request) {
        if (entityTagRepository.existsByTagIdAndEntityTypeAndEntityId(
                request.tagId(), request.entityType(), request.entityId())) {
            throw new ConflictException("Tag is already attached to this entity");
        }
        Tag tag = tagRepository.findById(request.tagId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag", request.tagId()));
        EntityTag entity = entityTagMapper.toEntity(request);
        entity.setTag(tag);
        EntityTag saved = entityTagRepository.save(entity);
        return entityTagMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EntityTagResponse findById(UUID id) {
        return entityTagMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EntityTagResponse> findByEntity(String entityType, UUID entityId) {
        return entityTagRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(entityTagMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EntityTagResponse> findByTag(UUID tagId) {
        return entityTagRepository.findByTagId(tagId).stream()
                .map(entityTagMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a tag from an entity")
    public void delete(UUID id) {
        EntityTag entity = getEntityOrThrow(id);
        entityTagRepository.delete(entity);
    }

    @Audited(action = "DELETE", description = "Remove all tags from an entity")
    public void deleteByEntity(String entityType, UUID entityId) {
        entityTagRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
    }

    private EntityTag getEntityOrThrow(UUID id) {
        return entityTagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EntityTag", id));
    }
}
