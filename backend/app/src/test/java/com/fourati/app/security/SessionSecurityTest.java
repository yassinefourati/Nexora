package com.fourati.app.security;

import com.fourati.identity.IdentityFacade;
import com.fourati.platform.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Covers the ownership check backing SessionController's
 * @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOwner(#id, authentication)")
 * — the line standing between "any authenticated user can revoke any other
 * user's session" (an IDOR) and correct self-service-only access.
 */
@ExtendWith(MockitoExtension.class)
class SessionSecurityTest {

    @Mock
    private IdentityFacade identity;

    @InjectMocks
    private SessionSecurity sessionSecurity;

    private static final UUID ME = UUID.randomUUID();

    private static Authentication person(UUID userId) {
        return new UsernamePasswordAuthenticationToken(new CurrentUser(userId, "sub-alice", "alice", false), null, List.of());
    }

    @Test
    void isOwner_sessionBelongsToCaller_returnsTrue() {
        UUID sessionId = UUID.randomUUID();
        when(identity.ownsSession(sessionId, ME)).thenReturn(true);

        assertThat(sessionSecurity.isOwner(sessionId, person(ME))).isTrue();
    }

    @Test
    void isOwner_sessionBelongsToSomeoneElse_returnsFalse() {
        UUID sessionId = UUID.randomUUID();
        when(identity.ownsSession(sessionId, ME)).thenReturn(false);

        assertThat(sessionSecurity.isOwner(sessionId, person(ME))).isFalse();
    }

    @Test
    void isOwner_principalWithoutCurrentUser_returnsFalse_neverAsksIdentity() {
        var auth = new UsernamePasswordAuthenticationToken("someone", "password");

        assertThat(sessionSecurity.isOwner(UUID.randomUUID(), auth)).isFalse();
        verifyNoInteractions(identity);
    }

    @Test
    void isOwner_apiKeyCaller_returnsFalse_neverAsksIdentity() {
        var auth = new UsernamePasswordAuthenticationToken(new CurrentUser(ME, null, "alice", true), null, List.of());

        assertThat(sessionSecurity.isOwner(UUID.randomUUID(), auth)).isFalse();
        verifyNoInteractions(identity);
    }
}
