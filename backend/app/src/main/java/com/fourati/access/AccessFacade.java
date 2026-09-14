package com.fourati.access;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * What other modules may ask the access domain (ADR 0004, ADR 0006).
 */
public interface AccessFacade {

    /** Cache of effective permission sets per sorted role-name set; cleared on {@link AccessChanged}. */
    String EFFECTIVE_PERMISSIONS_CACHE = "effectivePermissions";

    /** Cache of a user's effective permission set (database grants plus token roles); cleared on {@link AccessChanged}. */
    String USER_PERMISSIONS_CACHE = "userPermissions";

    /** Permissions granted through the given role names alone (the token's coarse roles). */
    List<PermissionResponse> effectivePermissionsForRoles(Collection<String> roleNames);

    /**
     * Permission codes ({@code resource.action}) a user holds right now: the
     * union of the database role grants that have not expired and the roles
     * named in their token. This is what becomes the caller's authorities.
     */
    Set<String> effectivePermissionCodes(UUID userId, Collection<String> roleNames);
}
