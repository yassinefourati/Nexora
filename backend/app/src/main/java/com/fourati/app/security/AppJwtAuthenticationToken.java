package com.fourati.app.security;

import com.fourati.platform.security.CurrentUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

/**
 * A {@link JwtAuthenticationToken} whose principal is the resolved
 * {@link CurrentUser} instead of the raw {@link Jwt}. Everything that accepted
 * a {@code JwtAuthenticationToken} before (claims, {@code getToken()}, the
 * subject as {@code getName()}) keeps working; {@code @AuthenticationPrincipal}
 * and {@code CurrentUser.from(authentication)} now yield the backend identity.
 */
public class AppJwtAuthenticationToken extends JwtAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final CurrentUser currentUser;

    public AppJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, CurrentUser currentUser) {
        super(jwt, authorities, jwt.getSubject());
        this.currentUser = currentUser;
    }

    @Override
    public Object getPrincipal() {
        return currentUser;
    }

    public CurrentUser currentUser() {
        return currentUser;
    }
}
