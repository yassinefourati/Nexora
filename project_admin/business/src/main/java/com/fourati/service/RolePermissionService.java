package com.fourati.service;

import com.fourati.domain.Permission;
import com.fourati.domain.Role;
import com.fourati.domain.RolePermission;
import com.fourati.dto.request.CreateRolePermissionRequest;
import com.fourati.dto.response.PermissionResponse;
import com.fourati.dto.response.RolePermissionResponse;
import com.fourati.mapper.PermissionMapper;
import com.fourati.mapper.RolePermissionMapper;
import com.fourati.repository.PermissionRepository;
import com.fourati.repository.RolePermissionRepository;
import com.fourati.repository.RoleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Audited(action = "CREATE", description = "Grant a permission to a role")
    public RolePermissionResponse create(CreateRolePermissionRequest request) {
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(request.roleId(), request.permissionId())) {
            throw new ConflictException("Role already has this permission assigned");
        }
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.roleId()));
        Permission permission = permissionRepository.findById(request.permissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", request.permissionId()));
        RolePermission entity = rolePermissionMapper.toEntity(request);
        entity.setRole(role);
        entity.setPermission(permission);
        RolePermission saved = rolePermissionRepository.save(entity);
        return rolePermissionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RolePermissionResponse findById(UUID id) {
        return rolePermissionMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<RolePermissionResponse> findAll(Pageable pageable) {
        return rolePermissionRepository.findAll(pageable).map(rolePermissionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RolePermissionResponse> findByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RolePermissionResponse> findByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId).stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> findEffectivePermissionsByRoleNames(Collection<String> roleNames) {
        if (roleNames.isEmpty()) {
            return List.of();
        }
        return rolePermissionRepository.findDistinctPermissionsByRoleNameIn(roleNames).stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Revoke a permission from a role")
    public void delete(UUID id) {
        RolePermission entity = getEntityOrThrow(id);
        rolePermissionRepository.delete(entity);
    }

    private RolePermission getEntityOrThrow(UUID id) {
        return rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RolePermission", id));
    }
}
