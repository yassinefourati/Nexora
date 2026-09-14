package com.fourati.app.security;

import com.fourati.access.AccessFacade;
import com.fourati.identity.IdentityFacade;
import com.fourati.identity.ProvisionedUser;
import com.fourati.platform.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a validated Keycloak JWT into the application's {@link AppJwtAuthenticationToken}.
 * This is the single place a bearer token becomes an identity (ADR 0006):
 *
 * <ul>
 *   <li>Authorities: {@code SCOPE_<scope>} from the standard scope claims,
 *       {@code ROLE_<ROLE>} from the {@code roles} claim (upper-cased, so the seeded
 *       lowercase role names still satisfy {@code hasRole('ADMIN')}), and one authority per
 *       effective permission code ({@code user.write}, ...) from the database role model, so
 *       {@code hasAuthority('user.write')} is the fine-grained check.</li>
 *   <li>Identity: the subject is linked to a backend {@code users} row, created
 *       just-in-time when missing, and the resulting {@link CurrentUser} becomes the
 *       principal. Ownership checks, audit rows and {@code /me} read it without
 *       another database round trip.</li>
 * </ul>
 *
 * A provisioning or permission-lookup failure is logged and yields a principal
 * without a user id or without permissions rather than failing authentication:
 * ownership checks then deny and {@code /me} reports not found, but a database
 * hiccup does not turn every request into a 401.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final IdentityFacade identity;
    private final AccessFacade access;
    private final JwtGrantedAuthoritiesConverter scopeConverter = scopeConverter();

    private static JwtGrantedAuthoritiesConverter scopeConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("SCOPE_");
        return converter;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> roles = roles(jwt);
        CurrentUser user = currentUser(jwt, roles);
        return new AppJwtAuthenticationToken(jwt, authorities(jwt, roles, user.permissions()), user);
    }

    static List<String> roles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles == null ? List.of() : roles;
    }

    Collection<GrantedAuthority> authorities(Jwt jwt, List<String> roles, Set<String> permissions) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeConverter.convert(jwt));
        roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).forEach(authorities::add);
        permissions.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
        return authorities;
    }

    CurrentUser currentUser(Jwt jwt, List<String> roles) {
        String subject = jwt.getSubject();
        ProvisionedUser provisioned;
        try {
            provisioned = identity.provision(jwt);
        } catch (RuntimeException ex) {
            log.error("User provisioning failed for subject {}: {}", subject, ex.getMessage());
            String username = jwt.getClaimAsString("preferred_username");
            return new CurrentUser(null, subject, username != null ? username : subject, false);
        }
        Set<String> permissions;
        try {
            permissions = access.effectivePermissionCodes(provisioned.id(), roles);
        } catch (RuntimeException ex) {
            log.error("Permission lookup failed for user {}: {}", provisioned.id(), ex.getMessage());
            permissions = Set.of();
        }
        return new CurrentUser(provisioned.id(), subject, provisioned.username(), false, permissions);
    }
}
