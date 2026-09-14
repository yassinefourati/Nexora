package com.fourati.app.security;

import com.fourati.identity.IdentityFacade;

import com.fourati.platform.error.ErrorCode;
import com.fourati.platform.error.Problems;
import com.fourati.platform.security.CurrentUser;
import com.fourati.identity.ApiKeyPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates programmatic callers presenting {@code X-Api-Key}. Runs before
 * the Bearer-token filter; a request carrying both headers is authenticated by
 * the API key and the Bearer token is ignored.
 *
 * Granted authorities are {@code SCOPE_<scope>} for each scope on the key. API
 * keys never receive {@code ROLE_ADMIN}: administrative endpoints stay reserved
 * for interactive Keycloak sessions. The principal is a {@link CurrentUser}
 * flagged as an API key, which ownership checks refuse, so a key can act on
 * its owner's rows only through scope-guarded endpoints.
 *
 * An invalid, revoked or expired key is rejected outright with a 401
 * problem-details body rather than falling through to anonymous, so a client
 * never silently loses its identity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Api-Key";

    private final IdentityFacade identity;
    private final Problems problems;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String rawKey = request.getHeader(HEADER);
        if (rawKey == null || rawKey.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        var principal = identity.authenticateApiKey(rawKey);
        if (principal.isEmpty()) {
            log.warn("Rejected request with invalid API key path={}", request.getRequestURI());
            problems.write(response, problems.of(ErrorCode.UNAUTHORIZED,
                    "Invalid, revoked or expired API key.", request.getRequestURI()));
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthentication(principal.get()));
        chain.doFilter(request, response);
    }

    /**
     * Authentication for a request proven by an API key. The principal is the
     * owning user as a {@link CurrentUser} (with {@code apiKey = true});
     * {@link #getDetails()} carries the key itself; {@code getName()} is the
     * owning user's username.
     */
    public static final class ApiKeyAuthentication extends AbstractAuthenticationToken {

        private static final long serialVersionUID = 1L;

        private final CurrentUser principal;

        ApiKeyAuthentication(ApiKeyPrincipal key) {
            super(toAuthorities(key.scopes()));
            this.principal = new CurrentUser(key.userId(), null, key.username(), true);
            setDetails(key);
            setAuthenticated(true);
        }

        private static List<GrantedAuthority> toAuthorities(List<String> scopes) {
            return scopes.stream()
                    .map(s -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + s))
                    .toList();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public CurrentUser getPrincipal() {
            return principal;
        }

        /** The API key that proved this request. */
        public ApiKeyPrincipal apiKey() {
            return (ApiKeyPrincipal) getDetails();
        }

        @Override
        public String getName() {
            return principal.username();
        }
    }
}
