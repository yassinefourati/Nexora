package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateOrganizationRequest;
import com.fourati.dto.request.UpdateOrganizationRequest;
import com.fourati.dto.response.OrganizationResponse;
import com.fourati.service.OrganizationService;
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
@RequestMapping(ApiConstants.VERSION + "/organizations")
@Tag(name = "Organizations", description = "Manage organizations (tenant-like groupings), which may be nested via a parent organization.")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new organization")
    public ResponseEntity<ApiResponse<OrganizationResponse>> create(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse created = organizationService.create(request);
        return ApiResponse.created(created, "Organization created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get an organization by id")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(organizationService.findById(id), "Organization retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List organizations (paginated)")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(organizationService.findAll(pageable), "Organizations retrieved");
    }

    @GetMapping("/{parentOrganizationId}/children")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List direct child organizations of a parent organization")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> listChildren(@PathVariable UUID parentOrganizationId) {
        return ApiResponse.ok(organizationService.findByParentOrganizationId(parentOrganizationId), "Child organizations retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an organization")
    public ResponseEntity<ApiResponse<OrganizationResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return ApiResponse.ok(organizationService.update(id, request), "Organization updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete an organization")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organizationService.delete(id);
        return ApiResponse.noContent();
    }
}
