package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateAppModuleRequest;
import com.fourati.dto.request.UpdateAppModuleRequest;
import com.fourati.dto.response.AppModuleResponse;
import com.fourati.service.AppModuleService;
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
@RequestMapping(ApiConstants.VERSION + "/app-modules")
@Tag(name = "App Modules", description = "Manage application modules (feature areas) that can be enabled or disabled.")
public class AppModuleController {

    private final AppModuleService appModuleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new application module")
    public ResponseEntity<ApiResponse<AppModuleResponse>> create(@Valid @RequestBody CreateAppModuleRequest request) {
        AppModuleResponse created = appModuleService.create(request);
        return ApiResponse.created(created, "App module created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an application module by id")
    public ResponseEntity<ApiResponse<AppModuleResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(appModuleService.findById(id), "App module retrieved");
    }

    @GetMapping
    @Operation(summary = "List application modules (paginated)")
    public ResponseEntity<ApiResponse<List<AppModuleResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(appModuleService.findAll(pageable), "App modules retrieved");
    }

    @GetMapping("/active")
    @Operation(summary = "List all active application modules")
    public ResponseEntity<ApiResponse<List<AppModuleResponse>>> listActive() {
        return ApiResponse.ok(appModuleService.findActive(), "Active app modules retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an application module")
    public ResponseEntity<ApiResponse<AppModuleResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateAppModuleRequest request) {
        return ApiResponse.ok(appModuleService.update(id, request), "App module updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an application module")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        appModuleService.delete(id);
        return ApiResponse.noContent();
    }
}
