package com.fourati.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the "confirmed not a bug" role-casing behavior: SecurityConfig's
 * JwtAuthenticationConverter must upper-case the "roles" JWT claim before prefixing
 * "ROLE_", so a seeded lowercase role name like "admin" (see the V9 migration) still
 * yields the ROLE_ADMIN authority expected by @PreAuthorize("hasRole('ADMIN')").
 *
 * SecurityConfig's converter-building method is private, so this test invokes it via
 * reflection on a real SecurityConfig instance rather than duplicating its logic --
 * that way a future change to the real method is what this test actually exercises.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private JwtAuthenticationConverter buildConverter() throws Exception {
        AppProperties appProperties = new AppProperties(
                new AppProperties.Async(5, 20, 100, "async-"),
                new AppProperties.Export(10_000),
                new AppProperties.Storage("uploads", 50),
                new AppProperties.Cors(List.of("*"), List.of("GET"), List.of("*"), false, 3600));
        SecurityConfig securityConfig = new SecurityConfig(appProperties);
        Method method = SecurityConfig.class.getDeclaredMethod("jwtAuthenticationConverter");
        method.setAccessible(true);
        return (JwtAuthenticationConverter) method.invoke(securityConfig);
    }

    private Jwt fakeJwtWithRoles(List<String> roles) {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("roles", roles)
                .subject("user-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void lowercaseSeededRoleClaim_producesUppercaseRoleAuthority() throws Exception {
        JwtAuthenticationConverter converter = buildConverter();
        Jwt jwt = fakeJwtWithRoles(List.of("admin"));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token).isNotNull();
        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).contains("ROLE_ADMIN");
        assertThat(authorities).doesNotContain("ROLE_admin");
    }

    @Test
    void missingRolesClaim_producesNoRoleAuthorities() throws Exception {
        JwtAuthenticationConverter converter = buildConverter();
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .subject("user-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).noneMatch(a -> a.startsWith("ROLE_"));
    }

    @Test
    void scopeClaim_stillProducesScopeAuthorities_alongsideRoles() throws Exception {
        JwtAuthenticationConverter converter = buildConverter();
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("roles", List.of("admin"))
                .claim("scope", "read write")
                .subject("user-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).contains("ROLE_ADMIN", "SCOPE_read", "SCOPE_write");
    }
}
