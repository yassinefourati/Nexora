package com.fourati.service;

import com.fourati.domain.Department;
import com.fourati.domain.Organization;
import com.fourati.dto.request.CreateDepartmentRequest;
import com.fourati.dto.request.UpdateDepartmentRequest;
import com.fourati.dto.response.DepartmentResponse;
import com.fourati.mapper.DepartmentMapper;
import com.fourati.repository.DepartmentRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentMapper departmentMapper;

    @Audited(action = "CREATE", description = "Create a new department")
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (request.code() != null
                && departmentRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new ConflictException("Department already exists with code: " + request.code()
                    + " in organization: " + request.organizationId());
        }
        Organization organization = getOrganizationOrThrow(request.organizationId());
        Department entity = departmentMapper.toEntity(request);
        entity.setOrganization(organization);
        if (request.parentDepartmentId() != null) {
            entity.setParentDepartment(getDepartmentOrThrow(request.parentDepartmentId()));
        }
        Department saved = departmentRepository.save(entity);
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(UUID id) {
        return departmentMapper.toResponse(getDepartmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(departmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findByOrganizationId(UUID organizationId) {
        return departmentRepository.findByOrganizationId(organizationId).stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findByParentDepartmentId(UUID parentDepartmentId) {
        return departmentRepository.findByParentDepartmentId(parentDepartmentId).stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update a department")
    public DepartmentResponse update(UUID id, UpdateDepartmentRequest request) {
        Department entity = getDepartmentOrThrow(id);
        if (request.code() != null && !request.code().equals(entity.getCode())
                && departmentRepository.existsByOrganizationIdAndCode(entity.getOrganization().getId(), request.code())) {
            throw new ConflictException("Department already exists with code: " + request.code()
                    + " in organization: " + entity.getOrganization().getId());
        }
        departmentMapper.updateEntityFromRequest(request, entity);
        if (request.parentDepartmentId() != null) {
            entity.setParentDepartment(getDepartmentOrThrow(request.parentDepartmentId()));
        } else {
            entity.setParentDepartment(null);
        }
        Department saved = departmentRepository.save(entity);
        return departmentMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a department")
    public void delete(UUID id) {
        Department entity = getDepartmentOrThrow(id);
        entity.setDeletedAt(Instant.now());
        departmentRepository.save(entity);
    }

    private Department getDepartmentOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private Organization getOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }
}
