package com.fourati.access.internal.application;

import com.fourati.identity.IdentityFacade;

import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.PermissionResponse;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.access.internal.persistence.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Query: what can a given user do right now? Resolves the user's current,
 * unexpired role grants (any organisation scope) to the distinct permission
 * set. {@code /me/permissions} answers this for the caller from the JWT roles;
 * this answers it for any user from the database, which is what an admin
 * needs when reviewing access.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EffectivePermissionsQuery {

    private final IdentityFacade identity;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionService rolePermissionService;

    public List<PermissionResponse> execute(UUID userId) {
        if (!identity.exists(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        Instant now = Instant.now();
        List<String> roleNames = userRoleRepository.findByUserId(userId).stream()
                .filter(ur -> ur.getExpiresAt() == null || ur.getExpiresAt().isAfter(now))
                .map(UserRole::getRole)
                .map(r -> r.getName())
                .distinct()
                .toList();
        return rolePermissionService.findEffectivePermissionsByRoleNames(roleNames);
    }
}
