package com.fourati.app.security;

import com.fourati.access.AccessFacade;
import com.fourati.identity.IdentityFacade;
import com.fourati.identity.ProvisionedUser;
import com.fourati.platform.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The converter is the single place a bearer token becomes an identity. It must
 * upper-case the seeded lowercase role names so {@code hasRole('ADMIN')} works,
 * keep scope authorities, resolve the backend user exactly once into the
 * {@link CurrentUser} principal, grant the effective permission codes as
 * authorities, and degrade (not fail) when either lookup is down.
 */
@ExtendWith(MockitoExtension.class)
class AppJwtAuthenticationConverterTest {

    @Mock private IdentityFacade identity;
    @Mock private AccessFacade access;

    @InjectMocks
    private AppJwtAuthenticationConverter converter;

    private static final UUID ID = UUID.randomUUID();

    private static Jwt.Builder jwt() {
        return Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .subject("sub-1")
                .claim("preferred_username", "alice-kc")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
    }

    private static List<String> authoritiesOf(AbstractAuthenticationToken token) {
        return token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    private void provisioned() {
        when(identity.provision(any())).thenReturn(new ProvisionedUser(ID, "alice"));
    }

    @Test
    void lowercaseSeededRoleClaim_producesUppercaseRoleAuthority() {
        provisioned();
        when(access.effectivePermissionCodes(eq(ID), anyList())).thenReturn(Set.of());

        AbstractAuthenticationToken token = converter.convert(jwt().claim("roles", List.of("admin")).build());

        assertThat(authoritiesOf(token)).contains("ROLE_ADMIN").doesNotContain("ROLE_admin");
    }

    @Test
    void scopeClaim_stillProducesScopeAuthorities_alongsideRoles() {
        provisioned();
        when(access.effectivePermissionCodes(eq(ID), anyList())).thenReturn(Set.of());

        AbstractAuthenticationToken token = converter.convert(
                jwt().claim("roles", List.of("admin")).claim("scope", "read write").build());

        assertThat(authoritiesOf(token)).contains("ROLE_ADMIN", "SCOPE_read", "SCOPE_write");
    }

    @Test
    void effectivePermissions_becomeAuthorities_andRideOnThePrincipal() {
        provisioned();
        when(access.effectivePermissionCodes(ID, List.of("user"))).thenReturn(Set.of("user.read", "user.write"));

        AbstractAuthenticationToken token = converter.convert(jwt().claim("roles", List.of("user")).build());

        assertThat(authoritiesOf(token)).contains("ROLE_USER", "user.read", "user.write");
        CurrentUser user = CurrentUser.from(token).orElseThrow();
        assertThat(user.userId()).isEqualTo(ID);
        assertThat(user.username()).isEqualTo("alice");
        assertThat(user.permissions()).containsExactlyInAnyOrder("user.read", "user.write");
        assertThat(user.hasPermission("user.write")).isTrue();
        assertThat(token.getName()).isEqualTo("sub-1");
    }

    @Test
    void provisioningFailure_yieldsAuthenticatedPrincipalWithoutUserIdOrPermissions() {
        when(identity.provision(any())).thenThrow(new IllegalStateException("db down"));

        AbstractAuthenticationToken token = converter.convert(jwt().claim("roles", List.of("user")).build());

        assertThat(token.isAuthenticated()).isTrue();
        assertThat(authoritiesOf(token)).containsExactly("ROLE_USER");
        CurrentUser user = CurrentUser.from(token).orElseThrow();
        assertThat(user.userId()).isNull();
        assertThat(user.username()).isEqualTo("alice-kc");
        assertThat(user.permissions()).isEmpty();
        verify(access, never()).effectivePermissionCodes(any(), any());
    }

    @Test
    void permissionLookupFailure_keepsRolesButGrantsNoPermissions() {
        provisioned();
        when(access.effectivePermissionCodes(eq(ID), anyList())).thenThrow(new IllegalStateException("redis down"));

        AbstractAuthenticationToken token = converter.convert(jwt().claim("roles", List.of("admin")).build());

        assertThat(authoritiesOf(token)).containsExactly("ROLE_ADMIN");
        assertThat(CurrentUser.from(token).orElseThrow().userId()).isEqualTo(ID);
    }
}
