package com.fourati.service;

import com.fourati.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Ownership check for self-service session endpoints (e.g. "revoke my own
 * session" from the Profile page). Used from @PreAuthorize expressions
 * alongside hasRole('ADMIN') so a non-admin user can only act on their own
 * sessions, resolved the same way MeController resolves identity — by the
 * JWT's preferred_username, never by trusting a client-supplied user id.
 */
@Component("sessionSecurity")
@RequiredArgsConstructor
public class SessionSecurity {

    private final SessionRepository sessionRepository;

    public boolean isOwner(UUID sessionId, Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return false;
        }
        String username = jwtAuth.getToken().getClaimAsString("preferred_username");
        return username != null && sessionRepository.existsByIdAndUserUsername(sessionId, username);
    }
}
