package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.PermissionResponse;
import com.fourati.dto.response.UserResponse;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.web.ApiResponse;
import com.fourati.service.RolePermissionService;
import com.fourati.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints scoped to the currently authenticated user, resolved from their
 * JWT rather than a path parameter. Exists mainly to collapse the
 * roles -> role-permissions -> permissions join the frontend previously did
 * client-side as N+1 requests into a single call, and to give the frontend a
 * direct way to resolve "the current user's backend row" without paginating
 * through /users and matching by username client-side (which silently broke
 * once an org had more users than a single fetched page).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/me")
@Tag(name = "Me", description = "Endpoints scoped to the current authenticated user.")
public class MeController {

    private final RolePermissionService rolePermissionService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the current user's backend record, resolved from their JWT username")
    public ResponseEntity<ApiResponse<UserResponse>> me(JwtAuthenticationToken authentication) {
        String username = authentication.getToken().getClaimAsString("preferred_username");
        UserResponse user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        return ApiResponse.ok(user, "Current user retrieved");
    }

    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List the current user's effective permissions, resolved from their JWT roles")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> myPermissions(JwtAuthenticationToken authentication) {
        Jwt jwt = authentication.getToken();
        List<String> roleNames = jwt.getClaimAsStringList("roles");
        List<PermissionResponse> permissions = rolePermissionService
                .findEffectivePermissionsByRoleNames(roleNames == null ? List.of() : roleNames);
        return ApiResponse.ok(permissions, "Effective permissions retrieved");
    }
}
