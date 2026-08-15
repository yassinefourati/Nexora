package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateApiKeyRequest;
import com.fourati.dto.request.UpdateApiKeyRequest;
import com.fourati.dto.response.ApiKeyCreatedResponse;
import com.fourati.dto.response.ApiKeyResponse;
import com.fourati.service.ApiKeyService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * API for programmatic API keys issued to users. Keys are revoked rather
 * than deleted.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/api-keys")
@Tag(name = "API Keys", description = "Manage programmatic API keys")
@PreAuthorize("hasRole('ADMIN')")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @Operation(summary = "Create a new API key",
            description = "The raw secret is returned exactly once in this response and cannot be retrieved again — copy it now.")
    public ResponseEntity<ApiResponse<ApiKeyCreatedResponse>> create(@Valid @RequestBody CreateApiKeyRequest request) {
        ApiKeyCreatedResponse created = apiKeyService.create(request);
        return ApiResponse.created(created, "API key created successfully — copy the secret now, it will not be shown again");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an API key by id")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(apiKeyService.findById(id));
    }

    @GetMapping
    @Operation(summary = "List API keys, optionally filtered by user")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> list(
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        Page<ApiKeyResponse> page = userId != null
                ? apiKeyService.findByUserId(userId, pageable)
                : apiKeyService.findAll(pageable);
        return ApiResponse.paged(page);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an API key (name, scopes, expiry, or revoke)")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateApiKeyRequest request) {
        return ApiResponse.ok(apiKeyService.update(id, request), "API key updated successfully");
    }

    @PatchMapping("/{id}/revoke")
    @Operation(summary = "Revoke an API key immediately")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> revoke(@PathVariable UUID id) {
        return ApiResponse.ok(apiKeyService.revoke(id), "API key revoked successfully");
    }
}
