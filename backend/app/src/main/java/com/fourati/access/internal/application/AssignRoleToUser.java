package com.fourati.access.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.identity.UserSummary;

import com.fourati.identity.IdentityFacade;

import com.fourati.access.internal.domain.Role;
import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.AssignRoleRequest;
import com.fourati.access.UserRoleResponse;
import com.fourati.platform.error.BusinessRuleException;
import com.fourati.platform.error.ErrorCode;
import com.fourati.access.AccessChanged;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.access.internal.persistence.RoleRepository;
import com.fourati.access.internal.persistence.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case: grant a role to a user, optionally scoped to an organisation and
 * with an expiry.
 *
 * Rules:
 *   - the user must exist and be active (suspended/inactive users cannot gain roles)
 *   - the role must exist
 *   - the same (user, role, organisation) grant cannot be made twice
 *   - an expiry, if given, must be in the future
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AssignRoleToUser {

    private final IdentityFacade identity;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.ROLE_EDIT)
    @Audited(action = "ASSIGN_ROLE", entityType = "user", description = "Assign a role to a user")
    public UserRoleResponse execute(UUID userId, AssignRoleRequest request) {
        UserSummary user = identity.find(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!user.isActive()) {
            throw new BusinessRuleException(ErrorCode.USER_NOT_ACTIVE,
                    "User " + user.username() + " is " + user.status().value() + " and cannot be granted roles");
        }
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", request.roleId()));
        if (request.expiresAt() != null && !request.expiresAt().isAfter(Instant.now())) {
            throw new BusinessRuleException(ErrorCode.ROLE_ASSIGNMENT_EXPIRED,
                    "expiresAt must be in the future");
        }
        if (userRoleRepository.existsByUserIdAndRoleIdAndOrganizationId(userId, role.getId(), request.organizationId())) {
            throw new BusinessRuleException(ErrorCode.ROLE_ALREADY_ASSIGNED,
                    "Role " + role.getName() + " is already assigned to this user for the given organization");
        }

        UserRole grant = new UserRole();
        grant.setUserId(userId);
        grant.setRole(role);
        grant.setOrganizationId(request.organizationId());
        grant.setExpiresAt(request.expiresAt());
        grant.setAssignedAt(Instant.now());
        UserRole saved = userRoleRepository.save(grant);
        eventPublisher.publishEvent(new AccessChanged(userId, role.getId(), "ROLE_ASSIGNED"));
        return userRoleMapper.toResponse(saved);
    }
}
