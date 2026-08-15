package com.fourati.service;

import com.fourati.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Covers the ownership check backing SessionController's
 * @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOwner(#id, authentication)")
 * — this is the one line standing between "any authenticated user can revoke
 * any other user's session" (an IDOR) and correct self-service-only access.
 */
@ExtendWith(MockitoExtension.class)
class SessionSecurityTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionSecurity sessionSecurity;

    private Jwt jwtFor(String username) {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("preferred_username", username)
                .subject("some-keycloak-subject-uuid")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void isOwner_sessionBelongsToCaller_returnsTrue() {
        UUID sessionId = UUID.randomUUID();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwtFor("alice"));
        when(sessionRepository.existsByIdAndUserUsername(sessionId, "alice")).thenReturn(true);

        assertThat(sessionSecurity.isOwner(sessionId, auth)).isTrue();
    }

    @Test
    void isOwner_sessionBelongsToSomeoneElse_returnsFalse() {
        UUID sessionId = UUID.randomUUID();
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwtFor("alice"));
        when(sessionRepository.existsByIdAndUserUsername(sessionId, "alice")).thenReturn(false);

        assertThat(sessionSecurity.isOwner(sessionId, auth)).isFalse();
    }

    @Test
    void isOwner_nonJwtAuthentication_returnsFalse_neverQueriesRepository() {
        UUID sessionId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken("someone", "password");

        assertThat(sessionSecurity.isOwner(sessionId, auth)).isFalse();
    }
}
