package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateFeatureFlagRequest;
import com.fourati.dto.request.UpdateFeatureFlagRequest;
import com.fourati.dto.response.FeatureFlagResponse;
import com.fourati.service.FeatureFlagService;
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
@RequestMapping(ApiConstants.VERSION + "/feature-flags")
@Tag(name = "Feature Flags", description = "Manage feature flags, optionally scoped to an organization.")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new feature flag")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> create(@Valid @RequestBody CreateFeatureFlagRequest request) {
        FeatureFlagResponse created = featureFlagService.create(request);
        return ApiResponse.created(created, "Feature flag created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a feature flag by id")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(featureFlagService.findById(id), "Feature flag retrieved");
    }

    @GetMapping
    @Operation(summary = "List feature flags (paginated)")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(featureFlagService.findAll(pageable), "Feature flags retrieved");
    }

    @GetMapping("/by-organization/{organizationId}")
    @Operation(summary = "List feature flags for a given organization")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> listByOrganization(
            @PathVariable UUID organizationId) {
        return ApiResponse.ok(featureFlagService.findByOrganization(organizationId), "Feature flags retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a feature flag")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateFeatureFlagRequest request) {
        return ApiResponse.ok(featureFlagService.update(id, request), "Feature flag updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a feature flag")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        featureFlagService.delete(id);
        return ApiResponse.noContent();
    }
}
