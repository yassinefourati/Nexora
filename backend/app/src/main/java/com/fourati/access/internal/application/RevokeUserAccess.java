package com.fourati.access.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.access.AccessChanged;
import com.fourati.access.RevokeAccessResponse;
import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.internal.persistence.UserRoleRepository;
import com.fourati.identity.IdentityFacade;
import com.fourati.identity.RevokedCredentials;
import com.fourati.platform.audit.Audited;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case: cut off a user completely, in one transaction.
 *
 * The identity module revokes every live session and API key and suspends the
 * account (which also fails with 404 for an unknown user); this module removes
 * every role grant. Both happen in the same transaction, so a failure on either
 * side leaves the user untouched. Previously this took three unrelated
 * endpoints and an admin who remembered all of them.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RevokeUserAccess {

    private final IdentityFacade identity;
    private final UserRoleRepository userRoleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.USER_DELETE)
    @Audited(action = "REVOKE_ACCESS", entityType = "user", description = "Revoke all access for a user")
    public RevokeAccessResponse execute(UUID userId) {
        RevokedCredentials revoked = identity.revokeCredentialsAndSuspend(userId);

        List<UserRole> roles = userRoleRepository.findByUserId(userId);
        userRoleRepository.deleteAll(roles);

        eventPublisher.publishEvent(new AccessChanged(userId, null, "ACCESS_REVOKED"));
        return new RevokeAccessResponse(userId, roles.size(), revoked.sessions(), revoked.apiKeys(), revoked.status());
    }
}
