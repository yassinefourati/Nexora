package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateRoleRequest;
import com.fourati.dto.request.UpdateRoleRequest;
import com.fourati.dto.response.RoleResponse;
import com.fourati.service.RoleService;
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
    @Operation(summary = "List roles (paginated)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(roleService.findAll(pageable), "Roles retrieved");
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
