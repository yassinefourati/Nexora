package com.fourati.service;

import com.fourati.domain.Organization;
import com.fourati.dto.request.CreateOrganizationRequest;
import com.fourati.dto.request.UpdateOrganizationRequest;
import com.fourati.dto.response.OrganizationResponse;
import com.fourati.mapper.OrganizationMapper;
import com.fourati.repository.OrganizationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Audited(action = "CREATE", description = "Create a new organization")
    public OrganizationResponse create(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCode(request.code())) {
            throw new ConflictException("Organization already exists with code: " + request.code());
        }
        Organization entity = organizationMapper.toEntity(request);
        if (request.parentOrganizationId() != null) {
            entity.setParentOrganization(getEntityOrThrow(request.parentOrganizationId()));
        }
        Organization saved = organizationRepository.save(entity);
        return organizationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponse findById(UUID id) {
        return organizationMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<OrganizationResponse> findAll(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findByParentOrganizationId(UUID parentOrganizationId) {
        return organizationRepository.findByParentOrganizationId(parentOrganizationId).stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update an organization")
    public OrganizationResponse update(UUID id, UpdateOrganizationRequest request) {
        Organization entity = getEntityOrThrow(id);
        if (!entity.getCode().equals(request.code()) && organizationRepository.existsByCode(request.code())) {
            throw new ConflictException("Organization already exists with code: " + request.code());
        }
        organizationMapper.updateEntityFromRequest(request, entity);
        if (request.parentOrganizationId() != null) {
            entity.setParentOrganization(getEntityOrThrow(request.parentOrganizationId()));
        } else {
            entity.setParentOrganization(null);
        }
        Organization saved = organizationRepository.save(entity);
        return organizationMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete an organization")
    public void delete(UUID id) {
        Organization entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        organizationRepository.save(entity);
    }

    private Organization getEntityOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }
}
