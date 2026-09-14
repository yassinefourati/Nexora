package com.fourati.access.internal.application;

import com.fourati.access.AccessFacade;
import com.fourati.access.PermissionResponse;
import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.internal.persistence.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Query: what can this user do right now? The union of the roles named in
 * their token (Keycloak's coarse roles, which also exist as database roles)
 * and the unexpired role grants the administration UI manages, resolved to
 * the distinct permission set (ADR 0006).
 *
 * Cached per (user, token roles) in the shared store and cleared by
 * {@code CacheInvalidationListener} on every {@code AccessChanged}, so a
 * revoked grant is denied on the next request on every replica.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPermissionsQuery {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionService rolePermissionService;

    @Cacheable(cacheNames = AccessFacade.USER_PERMISSIONS_CACHE, key = "#userId + ':' + new java.util.TreeSet(#roleNames)")
    public List<PermissionResponse> execute(UUID userId, Collection<String> roleNames) {
        Set<String> names = new TreeSet<>(roleNames == null ? List.of() : roleNames);
        if (userId != null) {
            Instant now = Instant.now();
            userRoleRepository.findByUserId(userId).stream()
                    .filter(grant -> grant.getExpiresAt() == null || grant.getExpiresAt().isAfter(now))
                    .map(UserRole::getRole)
                    .filter(role -> role != null && role.getName() != null)
                    .forEach(role -> names.add(role.getName()));
        }
        return rolePermissionService.findEffectivePermissionsByRoleNames(names);
    }
}
