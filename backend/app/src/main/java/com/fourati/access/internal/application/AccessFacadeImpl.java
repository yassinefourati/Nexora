package com.fourati.access.internal.application;

import com.fourati.access.AccessFacade;
import com.fourati.access.PermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** The access module's public face. Goes through the service and query beans so their cache annotations apply. */
@Component
@RequiredArgsConstructor
class AccessFacadeImpl implements AccessFacade {

    private final RolePermissionService rolePermissionService;
    private final UserPermissionsQuery userPermissionsQuery;

    @Override
    public List<PermissionResponse> effectivePermissionsForRoles(Collection<String> roleNames) {
        return rolePermissionService.findEffectivePermissionsByRoleNames(roleNames);
    }

    @Override
    public Set<String> effectivePermissionCodes(UUID userId, Collection<String> roleNames) {
        return userPermissionsQuery.execute(userId, roleNames).stream()
                .map(PermissionResponse::code)
                .collect(Collectors.toUnmodifiableSet());
    }
}
