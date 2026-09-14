package com.fourati.identity;

import com.fourati.platform.error.ResourceNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

/**
 * What other modules may ask the identity domain (ADR 0004). Users, identity
 * links, API keys and sessions are internal; everyone else holds a user id
 * and comes here to validate, describe or act on it.
 */
public interface IdentityFacade {

    /** Cache of just-in-time provisioning results, keyed by identity-provider subject. */
    String PROVISIONED_USERS_CACHE = "provisionedUsers";

    /** Resolves (and provisions when unknown) the backend user for a validated token. */
    ProvisionedUser provision(Jwt jwt);

    /** Resolves a raw {@code X-Api-Key} value; empty when unknown, revoked or expired. */
    Optional<ApiKeyPrincipal> authenticateApiKey(String rawKey);

    boolean exists(UUID userId);

    /** Fails with the standard 404 problem when the user does not exist. */
    default void ensureExists(UUID userId) {
        if (!exists(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
    }

    Optional<UserSummary> find(UUID userId);

    /** True when the session exists and belongs to {@code userId}. */
    boolean ownsSession(UUID sessionId, UUID userId);

    /**
     * Resolves an audit actor to a user id: the given id when it exists, else the
     * identity-provider subject through its link, else the username. Empty for
     * system actors and unknown names.
     */
    Optional<UUID> resolveActor(UUID userId, String subject, String username);

    /**
     * Revokes every live session and API key of the user and suspends the
     * account, publishing {@link IdentityChanged}. Part of the access-revocation
     * use case; role removal stays in the access module.
     */
    RevokedCredentials revokeCredentialsAndSuspend(UUID userId);
}
