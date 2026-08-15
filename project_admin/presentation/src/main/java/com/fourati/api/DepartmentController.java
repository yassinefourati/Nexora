package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateDepartmentRequest;
import com.fourati.dto.request.UpdateDepartmentRequest;
import com.fourati.dto.response.DepartmentResponse;
import com.fourati.service.DepartmentService;
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
@RequestMapping(ApiConstants.VERSION + "/departments")
@Tag(name = "Departments", description = "Manage departments within an organization, which may be nested via a parent department.")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse created = departmentService.create(request);
        return ApiResponse.created(created, "Department created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a department by id")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(departmentService.findById(id), "Department retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List departments (paginated), optionally filtered by organization or parent department")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> list(Pageable pageable,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID parentDepartmentId) {
        if (organizationId != null) {
            return ApiResponse.ok(departmentService.findByOrganizationId(organizationId), "Departments retrieved");
        }
        if (parentDepartmentId != null) {
            return ApiResponse.ok(departmentService.findByParentDepartmentId(parentDepartmentId), "Departments retrieved");
        }
        return ApiResponse.paged(departmentService.findAll(pageable), "Departments retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        return ApiResponse.ok(departmentService.update(id, request), "Department updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a department")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        departmentService.delete(id);
        return ApiResponse.noContent();
    }
}
