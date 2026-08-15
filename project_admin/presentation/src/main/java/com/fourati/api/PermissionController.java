package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreatePermissionRequest;
import com.fourati.dto.request.UpdatePermissionRequest;
import com.fourati.dto.response.PermissionResponse;
import com.fourati.service.PermissionService;
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
@RequestMapping(ApiConstants.VERSION + "/permissions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Permissions", description = "Manage fine-grained permissions identified by resource and action.")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@Valid @RequestBody CreatePermissionRequest request) {
        PermissionResponse created = permissionService.create(request);
        return ApiResponse.created(created, "Permission created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a permission by id")
    public ResponseEntity<ApiResponse<PermissionResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(permissionService.findById(id), "Permission retrieved");
    }

    @GetMapping
    @Operation(summary = "List permissions (paginated)")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(permissionService.findAll(pageable), "Permissions retrieved");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a permission's description")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ApiResponse.ok(permissionService.update(id, request), "Permission updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a permission")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        permissionService.delete(id);
        return ApiResponse.noContent();
    }
}
