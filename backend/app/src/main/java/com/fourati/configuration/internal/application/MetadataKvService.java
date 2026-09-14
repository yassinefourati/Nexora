package com.fourati.configuration.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.configuration.internal.domain.MetadataKv;
import com.fourati.configuration.CreateMetadataKvRequest;
import com.fourati.configuration.UpdateMetadataKvRequest;
import com.fourati.configuration.MetadataKvResponse;
import com.fourati.configuration.internal.persistence.MetadataKvRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MetadataKvService {

    private final MetadataKvRepository metadataKvRepository;
    private final MetadataKvMapper metadataKvMapper;

    @Audited(action = "CREATE", description = "Create a metadata key/value entry")
    public MetadataKvResponse create(CreateMetadataKvRequest request) {
        if (metadataKvRepository.existsByEntityTypeAndEntityIdAndKey(
                request.entityType(), request.entityId(), request.key())) {
            throw new ConflictException("Metadata key already exists: " + request.key());
        }
        MetadataKv entity = metadataKvMapper.toEntity(request);
        MetadataKv saved = metadataKvRepository.save(entity);
        return metadataKvMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MetadataKvResponse findById(UUID id) {
        return metadataKvMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<MetadataKvResponse> findByEntity(String entityType, UUID entityId) {
        return metadataKvRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(metadataKvMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<MetadataKvResponse> findAll(Pageable pageable) {
        return metadataKvRepository.findAll(pageable).map(metadataKvMapper::toResponse);
    }

    /** Server-side search over entityType, key (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<MetadataKvResponse> search(String q, Pageable pageable) {
        return metadataKvRepository.findAll(TextSearch.<MetadataKv>contains(q, "entityType", "key"), pageable).map(metadataKvMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MetadataKvResponse findByEntityAndKey(String entityType, UUID entityId, String key) {
        MetadataKv entity = metadataKvRepository.findByEntityTypeAndEntityIdAndKey(entityType, entityId, key)
                .orElseThrow(() -> new ResourceNotFoundException("MetadataKv", entityType + ":" + entityId + ":" + key));
        return metadataKvMapper.toResponse(entity);
    }

    @Audited(action = "UPDATE", description = "Update a metadata key/value entry")
    public MetadataKvResponse update(UUID id, UpdateMetadataKvRequest request) {
        MetadataKv entity = getEntityOrThrow(id);
        VersionCheck.check(MetadataKv.class, id, entity.getVersion(), request.version());
        metadataKvMapper.updateEntityFromRequest(request, entity);
        MetadataKv saved = metadataKvRepository.save(entity);
        return metadataKvMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a metadata key/value entry")
    public void delete(UUID id) {
        MetadataKv entity = getEntityOrThrow(id);
        metadataKvRepository.delete(entity);
    }

    private MetadataKv getEntityOrThrow(UUID id) {
        return metadataKvRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MetadataKv", id));
    }
}
