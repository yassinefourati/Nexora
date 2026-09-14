package com.fourati.access.internal.web;

import com.fourati.shared.api.ApiConstants;
import com.fourati.access.CreateRoleRequest;
import com.fourati.access.UpdateRoleRequest;
import com.fourati.access.RoleResponse;
import com.fourati.access.internal.application.RoleService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/roles")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Roles", description = "Manage roles that group permissions and can be assigned to users.")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roleService.create(request);
        return ApiResponse.created(created, "Role created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a role by id")
    public ResponseEntity<ApiResponse<RoleResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(roleService.findById(id), "Role retrieved");
    }

    @GetMapping
    @Operation(summary = "List roles (paginated); q searches name, description")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list(
            @RequestParam(required = false) String q,
            Pageable pageable) {
        return ApiResponse.paged(roleService.search(q, pageable), "Roles retrieved");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.ok(roleService.update(id, request), "Role updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ApiResponse.noContent();
    }
}
