package com.fourati.platform.security;

import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The identity of the caller, resolved exactly once when the request is
 * authenticated and carried as the {@link Authentication#getPrincipal() principal}
 * for the rest of the request (ADR 0006).
 *
 * <ul>
 *   <li>{@code userId}: the backend {@code users.id} the credentials map to. Null only
 *       when just-in-time provisioning failed; ownership checks then deny and the
 *       {@code /me} endpoint reports not found, but the request is not turned into a 500.</li>
 *   <li>{@code subject}: the identity-provider subject for JWT callers, null for API keys.</li>
 *   <li>{@code username}: the backend username (not the token claim), which is what
 *       WebSocket user destinations and audit rows key on.</li>
 *   <li>{@code apiKey}: true when authenticated by {@code X-Api-Key}. API keys never pass
 *       ownership checks: a key acts on its owner's rows only through scope-guarded endpoints.</li>
 *   <li>{@code permissions}: the effective permission codes ({@code resource.action}) the
 *       caller holds through the database role model, resolved at authentication and also
 *       granted as authorities so {@code hasAuthority('user.write')} works.</li>
 * </ul>
 *
 * Framework-agnostic on purpose: the platform module must not know how the
 * principal was built, only what it carries.
 */
public record CurrentUser(UUID userId, String subject, String username, boolean apiKey, Set<String> permissions) {

    public CurrentUser {
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }

    /** A principal without fine-grained permissions (API keys, or a caller whose lookup failed). */
    public CurrentUser(UUID userId, String subject, String username, boolean apiKey) {
        this(userId, subject, username, apiKey, Set.of());
    }

    public boolean hasPermission(String code) {
        return permissions.contains(code);
    }

    /** The caller, if the request was authenticated by a converter that produced a {@link CurrentUser}. */
    public static Optional<CurrentUser> from(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CurrentUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** The backend user id, when provisioning succeeded and the caller is a person, not an API key. */
    public static Optional<UUID> personId(Authentication authentication) {
        return from(authentication)
                .filter(user -> !user.apiKey())
                .map(CurrentUser::userId);
    }
}
