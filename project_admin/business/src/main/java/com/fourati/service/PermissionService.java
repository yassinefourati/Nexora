package com.fourati.service;

import com.fourati.domain.Permission;
import com.fourati.dto.request.CreatePermissionRequest;
import com.fourati.dto.request.UpdatePermissionRequest;
import com.fourati.dto.response.PermissionResponse;
import com.fourati.mapper.PermissionMapper;
import com.fourati.repository.PermissionRepository;
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
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Audited(action = "CREATE", description = "Create a new permission")
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByResourceAndAction(request.resource(), request.action())) {
            throw new ConflictException(
                    "Permission already exists for resource/action: " + request.resource() + "." + request.action());
        }
        Permission entity = permissionMapper.toEntity(request);
        Permission saved = permissionRepository.save(entity);
        return permissionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(UUID id) {
        return permissionMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PermissionResponse> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(permissionMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a permission")
    public PermissionResponse update(UUID id, UpdatePermissionRequest request) {
        Permission entity = getEntityOrThrow(id);
        permissionMapper.updateEntityFromRequest(request, entity);
        Permission saved = permissionRepository.save(entity);
        return permissionMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a permission")
    public void delete(UUID id) {
        Permission entity = getEntityOrThrow(id);
        permissionRepository.delete(entity);
    }

    private Permission getEntityOrThrow(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
    }
}
