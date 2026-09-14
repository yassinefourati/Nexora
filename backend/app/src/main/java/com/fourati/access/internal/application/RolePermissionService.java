package com.fourati.access.internal.application;

import com.fourati.access.AccessFacade;

import com.fourati.access.internal.domain.RolePermission;
import com.fourati.access.PermissionResponse;
import com.fourati.access.RolePermissionResponse;
import com.fourati.access.AccessChanged;
import com.fourati.access.internal.persistence.PermissionRepository;
import com.fourati.access.internal.persistence.RolePermissionRepository;
import com.fourati.access.internal.persistence.RoleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Role-permission grants. The effective-permission set per role combination
 * is cached ({@link #EFFECTIVE_PERMISSIONS_CACHE}); every change publishes an
 * {@link AccessChanged} event and the cache is cleared after commit on every
 * replica by {@code CacheInvalidationListener} (ADR 0008).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final ApplicationEventPublisher eventPublisher;

    public static final String EFFECTIVE_PERMISSIONS_CACHE = AccessFacade.EFFECTIVE_PERMISSIONS_CACHE;

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
    @Cacheable(cacheNames = EFFECTIVE_PERMISSIONS_CACHE, key = "new java.util.TreeSet(#roleNames).toString()")
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
        UUID roleId = entity.getRole() != null ? entity.getRole().getId() : null;
        rolePermissionRepository.delete(entity);
        eventPublisher.publishEvent(new AccessChanged(null, roleId, "PERMISSION_REVOKED"));
    }

    private RolePermission getEntityOrThrow(UUID id) {
        return rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RolePermission", id));
    }
}
