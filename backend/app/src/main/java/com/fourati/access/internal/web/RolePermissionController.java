package com.fourati.access.internal.web;

import com.fourati.shared.security.Authorize;

import com.fourati.access.CreateRolePermissionRequest;
import com.fourati.access.GrantPermissionsRequest;
import com.fourati.access.RolePermissionResponse;
import com.fourati.access.internal.application.GrantPermissionsToRole;
import com.fourati.access.internal.application.RolePermissionService;
import com.fourati.platform.error.ConflictException;
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
 * The {@code role_permissions} join table as a resource, kept for inspection.
 * Its write endpoints are retired in favour of the use-case endpoints on
 * {@link RbacController} ({@code POST /roles/{roleId}/permissions},
 * {@code DELETE /roles/{roleId}/permissions/{permissionId}}).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/role-permissions")
@PreAuthorize(Authorize.ROLE_READ)
@Tag(name = "Role Permissions", description = "Inspect permission grants; write through /roles/{roleId}/permissions.")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;
    private final GrantPermissionsToRole grantPermissionsToRole;

    @PostMapping
    @PreAuthorize(Authorize.ROLE_EDIT)
    @SunsetEndpoint(since = "2026-09-13", sunset = "2027-03-31", successor = "/api/v1/roles/{roleId}/permissions")
    @Operation(summary = "Deprecated: use POST /roles/{roleId}/permissions", deprecated = true)
    public ResponseEntity<ApiResponse<RolePermissionResponse>> create(
            @Valid @RequestBody CreateRolePermissionRequest request) {
        List<RolePermissionResponse> granted = grantPermissionsToRole.execute(request.roleId(),
                new GrantPermissionsRequest(List.of(request.permissionId())));
        if (granted.isEmpty()) {
            throw new ConflictException("Role already has this permission assigned");
        }
        return ApiResponse.created(granted.get(0), "Permission granted");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a permission grant by id")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(rolePermissionService.findById(id), "Role permission retrieved");
    }

    @GetMapping
    @Operation(summary = "List all permission grants (paginated)")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(rolePermissionService.findAll(pageable), "Role permissions retrieved");
    }

    @GetMapping("/by-role/{roleId}")
    @Operation(summary = "List the permissions granted to a role")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> listByRole(@PathVariable UUID roleId) {
        return ApiResponse.ok(rolePermissionService.findByRoleId(roleId), "Role permissions retrieved");
    }

    @GetMapping("/by-permission/{permissionId}")
    @Operation(summary = "List the roles holding a permission")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> listByPermission(
            @PathVariable UUID permissionId) {
        return ApiResponse.ok(rolePermissionService.findByPermissionId(permissionId), "Role permissions retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @SunsetEndpoint(since = "2026-09-13", sunset = "2027-03-31", successor = "/api/v1/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Deprecated: use DELETE /roles/{roleId}/permissions/{permissionId}", deprecated = true)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        rolePermissionService.delete(id);
        return ApiResponse.noContent();
    }
}
