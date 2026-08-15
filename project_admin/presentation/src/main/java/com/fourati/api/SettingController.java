package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateSettingRequest;
import com.fourati.dto.request.UpdateSettingRequest;
import com.fourati.dto.response.SettingResponse;
import com.fourati.service.SettingService;
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
@RequestMapping(ApiConstants.VERSION + "/settings")
@Tag(name = "Settings", description = "Manage global and organization-scoped configuration settings.")
public class SettingController {

    private final SettingService settingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new setting")
    public ResponseEntity<ApiResponse<SettingResponse>> create(@Valid @RequestBody CreateSettingRequest request) {
        SettingResponse created = settingService.create(request);
        return ApiResponse.created(created, "Setting created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a setting by id")
    public ResponseEntity<ApiResponse<SettingResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(settingService.findById(id), "Setting retrieved");
    }

    @GetMapping
    @Operation(summary = "List settings (paginated)")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(settingService.findAll(pageable), "Settings retrieved");
    }

    @GetMapping("/by-scope/{scope}")
    @Operation(summary = "List settings for a given scope")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> listByScope(@PathVariable String scope) {
        return ApiResponse.ok(settingService.findByScope(scope), "Settings retrieved");
    }

    @GetMapping("/by-scope/{scope}/organization/{organizationId}")
    @Operation(summary = "List settings for a given scope and organization")
    public ResponseEntity<ApiResponse<List<SettingResponse>>> listByScopeAndOrganization(
            @PathVariable String scope, @PathVariable UUID organizationId) {
        return ApiResponse.ok(settingService.findByScopeAndOrganization(scope, organizationId), "Settings retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a setting")
    public ResponseEntity<ApiResponse<SettingResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateSettingRequest request) {
        return ApiResponse.ok(settingService.update(id, request), "Setting updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a setting")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        settingService.delete(id);
        return ApiResponse.noContent();
    }
}
