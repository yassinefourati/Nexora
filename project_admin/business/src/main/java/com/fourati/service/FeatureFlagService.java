package com.fourati.service;

import com.fourati.domain.FeatureFlag;
import com.fourati.domain.Organization;
import com.fourati.dto.request.CreateFeatureFlagRequest;
import com.fourati.dto.request.UpdateFeatureFlagRequest;
import com.fourati.dto.response.FeatureFlagResponse;
import com.fourati.mapper.FeatureFlagMapper;
import com.fourati.repository.FeatureFlagRepository;
import com.fourati.repository.OrganizationRepository;
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
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final OrganizationRepository organizationRepository;
    private final FeatureFlagMapper featureFlagMapper;

    @Audited(action = "CREATE", description = "Create a new feature flag")
    public FeatureFlagResponse create(CreateFeatureFlagRequest request) {
        boolean exists = request.organizationId() == null
                ? featureFlagRepository.existsByKeyAndOrganizationIdIsNull(request.key())
                : featureFlagRepository.existsByKeyAndOrganizationId(request.key(), request.organizationId());
        if (exists) {
            throw new ConflictException("Feature flag already exists with key: " + request.key());
        }
        FeatureFlag entity = featureFlagMapper.toEntity(request);
        if (request.organizationId() != null) {
            entity.setOrganization(getOrganizationOrThrow(request.organizationId()));
        }
        FeatureFlag saved = featureFlagRepository.save(entity);
        return featureFlagMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeatureFlagResponse findById(UUID id) {
        return featureFlagMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FeatureFlagResponse> findAll(Pageable pageable) {
        return featureFlagRepository.findAll(pageable).map(featureFlagMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> findByOrganization(UUID organizationId) {
        return featureFlagRepository.findByOrganizationId(organizationId).stream()
                .map(featureFlagMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update a feature flag")
    public FeatureFlagResponse update(UUID id, UpdateFeatureFlagRequest request) {
        FeatureFlag entity = getEntityOrThrow(id);
        featureFlagMapper.updateEntityFromRequest(request, entity);
        FeatureFlag saved = featureFlagRepository.save(entity);
        return featureFlagMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a feature flag")
    public void delete(UUID id) {
        FeatureFlag entity = getEntityOrThrow(id);
        featureFlagRepository.delete(entity);
    }

    private FeatureFlag getEntityOrThrow(UUID id) {
        return featureFlagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", id));
    }

    private Organization getOrganizationOrThrow(UUID organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));
    }
}
