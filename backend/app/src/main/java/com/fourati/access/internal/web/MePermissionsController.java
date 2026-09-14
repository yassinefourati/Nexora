package com.fourati.access.internal.web;

import com.fourati.access.PermissionResponse;
import com.fourati.access.internal.application.UserPermissionsQuery;
import com.fourati.platform.security.CurrentUser;
import com.fourati.platform.web.ApiResponse;
import com.fourati.shared.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * {@code GET /me/permissions}: the effective permission set of the caller's
 * roles, collapsing roles → role-permissions → permissions into one call. Lives
 * in the access module because that is who owns the answer; the {@code /me}
 * prefix is shared with the identity module's {@code MeController}.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Me", description = "Endpoints scoped to the current authenticated user.")
public class MePermissionsController {

    private final UserPermissionsQuery userPermissionsQuery;

    @GetMapping(ApiConstants.VERSION + "/me/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List the current user's effective permissions: database role grants plus JWT roles")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> myPermissions(
            @AuthenticationPrincipal CurrentUser me, JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        List<String> roleNames = jwt.getClaimAsStringList("roles");
        List<PermissionResponse> permissions = userPermissionsQuery.execute(
                me == null ? null : me.userId(), roleNames == null ? List.of() : roleNames);
        return ApiResponse.ok(permissions, "Effective permissions retrieved");
    }
}
