package com.fourati.shared.security;

/**
 * The {@code @PreAuthorize} expressions of the application, in one place
 * (ADR 0006). Keycloak's {@code ADMIN} role is the coarse short-circuit; the
 * fine-grained permission codes ({@code resource.action}, see the
 * {@code permissions} table) are granted as authorities when the request is
 * authenticated and are what the administration UI actually edits.
 *
 * A use case names the permission it needs; its endpoint repeats the same
 * expression so the OpenAPI document and the HTTP layer tell the same story.
 */
public final class Authorize {

    private Authorize() {
    }

    public static final String AUTHENTICATED = "isAuthenticated()";
    public static final String ADMIN = "hasRole('ADMIN')";

    public static final String USER_READ = ADMIN + " or hasAuthority('user.read')";
    public static final String USER_WRITE = ADMIN + " or hasAuthority('user.write')";
    public static final String USER_EDIT = ADMIN + " or hasAuthority('user.edit')";
    public static final String USER_DELETE = ADMIN + " or hasAuthority('user.delete')";

    public static final String ROLE_READ = ADMIN + " or hasAuthority('role.read')";
    public static final String ROLE_WRITE = ADMIN + " or hasAuthority('role.write')";
    public static final String ROLE_EDIT = ADMIN + " or hasAuthority('role.edit')";
    public static final String ROLE_DELETE = ADMIN + " or hasAuthority('role.delete')";
}
