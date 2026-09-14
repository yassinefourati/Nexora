package com.fourati.access.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.access.AccessChanged;
import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.internal.persistence.UserRoleRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case: take a role away from a user, in every organization scope it was
 * granted for. 404 when the user holds no such grant, so a mistaken call is
 * visible instead of silently succeeding.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RemoveRoleFromUser {

    private final UserRoleRepository userRoleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.ROLE_EDIT)
    @Audited(action = "REMOVE_ROLE", entityType = "user", description = "Remove a role from a user")
    public void execute(UUID userId, UUID roleId) {
        List<UserRole> grants = userRoleRepository.findByUserId(userId).stream()
                .filter(g -> g.getRole() != null && roleId.equals(g.getRole().getId()))
                .toList();
        if (grants.isEmpty()) {
            throw new ResourceNotFoundException("UserRole", userId + "/" + roleId);
        }
        userRoleRepository.deleteAll(grants);
        eventPublisher.publishEvent(new AccessChanged(userId, roleId, "ROLE_REMOVED"));
    }
}
