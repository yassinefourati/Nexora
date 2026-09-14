package com.fourati.access.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.access.AccessChanged;
import com.fourati.access.internal.domain.RolePermission;
import com.fourati.access.internal.persistence.RolePermissionRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Use case: take a permission away from a role. 404 when the role does not hold it. */
@Service
@RequiredArgsConstructor
@Transactional
public class RevokePermissionFromRole {

    private final RolePermissionRepository rolePermissionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.ROLE_EDIT)
    @Audited(action = "REVOKE_PERMISSION", entityType = "role", description = "Revoke a permission from a role")
    public void execute(UUID roleId, UUID permissionId) {
        List<RolePermission> grants = rolePermissionRepository.findByRoleId(roleId).stream()
                .filter(g -> g.getPermission() != null && permissionId.equals(g.getPermission().getId()))
                .toList();
        if (grants.isEmpty()) {
            throw new ResourceNotFoundException("RolePermission", roleId + "/" + permissionId);
        }
        rolePermissionRepository.deleteAll(grants);
        eventPublisher.publishEvent(new AccessChanged(null, roleId, "PERMISSION_REVOKED"));
    }
}
