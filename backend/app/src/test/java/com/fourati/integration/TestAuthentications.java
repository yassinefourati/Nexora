package com.fourati.integration;

import com.fourati.app.security.AppJwtAuthenticationToken;
import com.fourati.identity.internal.domain.User;
import com.fourati.platform.security.CurrentUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Builds the exact {@link AppJwtAuthenticationToken} the real
 * {@code AppJwtAuthenticationConverter} produces, so HTTP-level tests exercise
 * ownership checks, audit actors and {@code /me} against a {@link CurrentUser}
 * principal rather than a raw JWT. Spring Security's {@code jwt()} post-processor
 * bypasses the converter and would leave the principal as the token.
 */
public final class TestAuthentications {

    private TestAuthentications() {
    }

    /** A seeded backend user, with the given Keycloak roles. */
    public static RequestPostProcessor asUser(User user, String... roles) {
        return authentication(token(user.getId(), user.getUsername(), roles));
    }

    /** A person Keycloak knows but who has no backend row (provisioning is bypassed in tests). */
    public static RequestPostProcessor asPerson(String username, String... roles) {
        return authentication(token(null, username, roles));
    }

    public static AppJwtAuthenticationToken token(UUID userId, String username, String... roles) {
        String subject = "sub-" + username;
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", username)
                .claim("roles", List.of(roles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        List<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .toList();
        return new AppJwtAuthenticationToken(jwt, authorities, new CurrentUser(userId, subject, username, false));
    }
}
