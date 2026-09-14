package com.fourati.access.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.access.internal.domain.Permission;
import com.fourati.access.internal.domain.Role;
import com.fourati.access.internal.domain.RolePermission;
import com.fourati.access.GrantPermissionsRequest;
import com.fourati.access.RolePermissionResponse;
import com.fourati.access.AccessChanged;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.access.internal.persistence.PermissionRepository;
import com.fourati.access.internal.persistence.RolePermissionRepository;
import com.fourati.access.internal.persistence.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Use case: grant a set of permissions to a role in one transaction.
 *
 * Idempotent: permissions the role already holds are skipped. Unknown
 * permission ids fail the whole call (404) before anything is written, so a
 * typo never results in a half-applied grant. One audit event per call, not
 * one per permission.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GrantPermissionsToRole {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RolePermissionMapper rolePermissionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.ROLE_EDIT)
    @Audited(action = "GRANT_PERMISSIONS", entityType = "role", description = "Grant permissions to a role")
    public List<RolePermissionResponse> execute(UUID roleId, GrantPermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        List<UUID> wanted = new ArrayList<>(new LinkedHashSet<>(request.permissionIds()));
        Map<UUID, Permission> found = permissionRepository.findAllById(wanted).stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));
        for (UUID id : wanted) {
            if (!found.containsKey(id)) {
                throw new ResourceNotFoundException("Permission", id);
            }
        }

        List<RolePermission> created = new ArrayList<>();
        for (UUID id : wanted) {
            if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, id)) {
                continue;
            }
            RolePermission grant = new RolePermission();
            grant.setRole(role);
            grant.setPermission(found.get(id));
            created.add(grant);
        }
        List<RolePermissionResponse> saved = rolePermissionRepository.saveAll(created).stream()
                .map(rolePermissionMapper::toResponse)
                .toList();
        if (!created.isEmpty()) {
            eventPublisher.publishEvent(new AccessChanged(null, roleId, "PERMISSIONS_GRANTED"));
        }
        return saved;
    }
}
