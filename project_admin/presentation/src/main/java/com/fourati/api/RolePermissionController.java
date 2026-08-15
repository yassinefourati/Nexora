package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateRolePermissionRequest;
import com.fourati.dto.response.RolePermissionResponse;
import com.fourati.service.RolePermissionService;
import com.fourati.platform.web.ApiResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/role-permissions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Role Permissions", description = "Manage permission grants assigned to roles.")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping
    @Operation(summary = "Grant a permission to a role")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> create(
            @Valid @RequestBody CreateRolePermissionRequest request) {
        RolePermissionResponse created = rolePermissionService.create(request);
        return ApiResponse.created(created, "Permission granted to role successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a role-permission grant by id")
    public ResponseEntity<ApiResponse<RolePermissionResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(rolePermissionService.findById(id), "Role permission retrieved");
    }

    @GetMapping
    @Operation(summary = "List role-permission grants (paginated)")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(rolePermissionService.findAll(pageable), "Role permissions retrieved");
    }

    @GetMapping("/by-role/{roleId}")
    @Operation(summary = "List permissions granted to a role")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> listByRole(@PathVariable UUID roleId) {
        return ApiResponse.ok(rolePermissionService.findByRoleId(roleId), "Role permissions retrieved");
    }

    @GetMapping("/by-permission/{permissionId}")
    @Operation(summary = "List roles that have a given permission")
    public ResponseEntity<ApiResponse<List<RolePermissionResponse>>> listByPermission(
            @PathVariable UUID permissionId) {
        return ApiResponse.ok(rolePermissionService.findByPermissionId(permissionId), "Role permissions retrieved");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke a permission from a role")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        rolePermissionService.delete(id);
        return ApiResponse.noContent();
    }
}
