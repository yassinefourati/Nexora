package com.fourati.access.internal.web;

import com.fourati.shared.security.Authorize;

import com.fourati.access.AssignRoleRequest;
import com.fourati.access.GrantPermissionsRequest;
import com.fourati.access.PermissionResponse;
import com.fourati.access.RevokeAccessResponse;
import com.fourati.access.RolePermissionResponse;
import com.fourati.access.UserRoleResponse;
import com.fourati.access.internal.application.AssignRoleToUser;
import com.fourati.access.internal.application.EffectivePermissionsQuery;
import com.fourati.access.internal.application.GrantPermissionsToRole;
import com.fourati.access.internal.application.RemoveRoleFromUser;
import com.fourati.access.internal.application.RevokePermissionFromRole;
import com.fourati.access.internal.application.RevokeUserAccess;
import com.fourati.platform.web.ApiResponse;
import com.fourati.shared.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * Access-management operations expressed as the actions an administrator
 * performs, rather than as rows in join tables. The generic
 * {@code /user-roles} and {@code /role-permissions} resources remain for
 * inspection; these endpoints are the write path (ADR 0004, use cases).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION)
@Tag(name = "Access Management", description = "Grant, inspect and revoke user access as business operations.")
public class RbacController {

    private final AssignRoleToUser assignRoleToUser;
    private final RemoveRoleFromUser removeRoleFromUser;
    private final GrantPermissionsToRole grantPermissionsToRole;
    private final RevokePermissionFromRole revokePermissionFromRole;
    private final RevokeUserAccess revokeUserAccess;
    private final EffectivePermissionsQuery effectivePermissionsQuery;

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @Operation(summary = "Assign a role to a user (optionally scoped to an organization, optionally expiring)")
    public ResponseEntity<ApiResponse<UserRoleResponse>> assignRole(@PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request) {
        return ApiResponse.created(assignRoleToUser.execute(userId, request), "Role assigned");
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @Operation(summary = "Remove a role from a user, in every organization scope it was granted for")
    public ResponseEntity<Void> removeRole(@PathVariable UUID userId, @PathVariable UUID roleId) {
        removeRoleFromUser.execute(userId, roleId);
        return ApiResponse.noContent();
    }

    @GetMapping("/users/{userId}/effective-permissions")
    @PreAuthorize(Authorize.ROLE_READ)
    @Operation(summary = "Resolve every permission a user currently holds through unexpired role grants")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> effectivePermissions(@PathVariable UUID userId) {
        return ApiResponse.ok(effectivePermissionsQuery.execute(userId), "Effective permissions resolved");
    }

    @PostMapping("/users/{userId}/access:revoke")
    @PreAuthorize(Authorize.USER_DELETE)
    @Operation(summary = "Revoke all access: remove roles, revoke sessions and API keys, suspend the account")
    public ResponseEntity<ApiResponse<RevokeAccessResponse>> revokeAccess(@PathVariable UUID userId) {
        return ApiResponse.ok(revokeUserAccess.execute(userId), "Access revoked");
    }

    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @Operation(summary = "Grant several permissions to a role in one transaction (already-granted ones are skipped)")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> grantPermissions(@PathVariable UUID roleId,
            @Valid @RequestBody GrantPermissionsRequest request) {
        return ApiResponse.created(grantPermissionsToRole.execute(roleId, request), "Permissions granted");
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize(Authorize.ROLE_EDIT)
    @Operation(summary = "Revoke a permission from a role")
    public ResponseEntity<Void> revokePermission(@PathVariable UUID roleId, @PathVariable UUID permissionId) {
        revokePermissionFromRole.execute(roleId, permissionId);
        return ApiResponse.noContent();
    }
}
