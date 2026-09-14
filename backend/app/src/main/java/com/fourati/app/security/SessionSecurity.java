package com.fourati.app.security;

import com.fourati.identity.IdentityFacade;
import com.fourati.platform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Ownership check for self-service session endpoints (e.g. "revoke my own
 * session" from the Profile page). Used from @PreAuthorize expressions
 * alongside hasRole('ADMIN') so a non-admin user can only act on their own
 * sessions. Identity is the {@link CurrentUser} principal resolved at
 * authentication (ADR 0006), never a client-supplied user id and never an
 * API key; the session itself is asked of the identity module's facade.
 */
@Component("sessionSecurity")
@RequiredArgsConstructor
public class SessionSecurity {

    private final IdentityFacade identity;

    public boolean isOwner(UUID sessionId, Authentication authentication) {
        return CurrentUser.personId(authentication)
                .map(me -> identity.ownsSession(sessionId, me))
                .orElse(false);
    }
}
