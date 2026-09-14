package com.fourati.access.internal.web;

import com.fourati.shared.security.Authorize;

import com.fourati.access.AssignRoleRequest;
import com.fourati.access.CreateUserRoleRequest;
import com.fourati.access.UserRoleResponse;
import com.fourati.access.internal.application.AssignRoleToUser;
import com.fourati.access.internal.application.UserRoleService;
import com.fourati.platform.web.ApiResponse;
import com.fourati.platform.web.SunsetEndpoint;
import com.fourati.shared.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * The {@code user_roles} join table as a resource, kept for inspection. Its
 * write endpoints are retired in favour of the use-case endpoints on
 * {@link RbacController} ({@code POST /users/{userId}/roles},
 * {@code DELETE /users/{userId}/roles/{roleId}}) and announce it with
 * Deprecation, Sunset and Link headers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/user-roles")
@Tag(name = "User Roles", description = "Inspect role assignments; write through /users/{userId}/roles.")
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final AssignRoleToUser assignRoleToUser;

    @PostMapping
    @PreAuthorize(Authorize.ROLE_EDIT)
    @SunsetEndpoint(since = "2026-09-13", sunset = "2027-03-31", successor = "/api/v1/users/{userId}/roles")
    @Operation(summary = "Deprecated: use POST /users/{userId}/roles", deprecated = true)
    public ResponseEntity<ApiResponse<UserRoleResponse>> create(@Valid @RequestBody CreateUserRoleRequest request) {
        UserRoleResponse created = assignRoleToUser.execute(request.userId(),
                new AssignRoleRequest(request.roleId(), request.organizationId(), request.expiresAt()));
        return ApiResponse.created(created, "Role assigned");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a role assignment by id")
    public ResponseEntity<ApiResponse<UserRoleResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userRoleService.findById(id), "User role retrieved");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all role assignments (paginated)")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(userRoleService.findAll(pageable), "User roles retrieved");
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List the roles assigned to a user")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listByUser(@PathVariable UUID userId) {
        return ApiResponse.ok(userRoleService.findByUserId(userId), "User roles retrieved");
    }

    @GetMapping("/by-role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List the users holding a role (paginated)")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listByRole(@PathVariable UUID roleId, Pageable pageable) {
        return ApiResponse.paged(userRoleService.findByRoleId(roleId, pageable), "User roles retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @SunsetEndpoint(since = "2026-09-13", sunset = "2027-03-31", successor = "/api/v1/users/{userId}/roles/{roleId}")
    @Operation(summary = "Deprecated: use DELETE /users/{userId}/roles/{roleId}", deprecated = true)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userRoleService.delete(id);
        return ApiResponse.noContent();
    }
}
