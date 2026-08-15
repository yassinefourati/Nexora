package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUserRoleRequest;
import com.fourati.dto.response.UserRoleResponse;
import com.fourati.service.UserRoleService;
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
@RequestMapping(ApiConstants.VERSION + "/user-roles")
@Tag(name = "User Roles", description = "Manage role assignments granted to users, optionally scoped to an organization.")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a role to a user")
    public ResponseEntity<ApiResponse<UserRoleResponse>> create(@Valid @RequestBody CreateUserRoleRequest request) {
        UserRoleResponse created = userRoleService.create(request);
        return ApiResponse.created(created, "Role assigned to user successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a user-role assignment by id")
    public ResponseEntity<ApiResponse<UserRoleResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userRoleService.findById(id), "User role retrieved");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List user-role assignments (paginated)")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(userRoleService.findAll(pageable), "User roles retrieved");
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List roles assigned to a user")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listByUser(@PathVariable UUID userId) {
        return ApiResponse.ok(userRoleService.findByUserId(userId), "User roles retrieved");
    }

    @GetMapping("/by-role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users assigned to a role (paginated)")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> listByRole(@PathVariable UUID roleId, Pageable pageable) {
        return ApiResponse.paged(userRoleService.findByRoleId(roleId, pageable), "User roles retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a role assignment from a user")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userRoleService.delete(id);
        return ApiResponse.noContent();
    }
}
