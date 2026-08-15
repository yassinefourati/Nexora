package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateMetadataKvRequest;
import com.fourati.dto.request.UpdateMetadataKvRequest;
import com.fourati.dto.response.MetadataKvResponse;
import com.fourati.service.MetadataKvService;
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
@RequestMapping(ApiConstants.VERSION + "/metadata")
@Tag(name = "Metadata", description = "Key/value metadata attached to arbitrary polymorphic entities.")
public class MetadataKvController {

    private final MetadataKvService metadataKvService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a metadata key/value entry")
    public ResponseEntity<ApiResponse<MetadataKvResponse>> create(@Valid @RequestBody CreateMetadataKvRequest request) {
        MetadataKvResponse created = metadataKvService.create(request);
        return ApiResponse.created(created, "Metadata entry created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a metadata entry by id")
    public ResponseEntity<ApiResponse<MetadataKvResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(metadataKvService.findById(id), "Metadata entry retrieved");
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List metadata entries (paginated), or filter to a single entity's entries")
    public ResponseEntity<ApiResponse<List<MetadataKvResponse>>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            Pageable pageable) {
        if (entityType != null && entityId != null) {
            return ApiResponse.ok(metadataKvService.findByEntity(entityType, entityId), "Metadata entries retrieved");
        }
        return ApiResponse.paged(metadataKvService.findAll(pageable), "Metadata entries retrieved");
    }

    @GetMapping("/lookup")
    @Operation(summary = "Get a metadata entry by entity and key")
    public ResponseEntity<ApiResponse<MetadataKvResponse>> getByEntityAndKey(
            @RequestParam String entityType, @RequestParam UUID entityId, @RequestParam String key) {
        return ApiResponse.ok(metadataKvService.findByEntityAndKey(entityType, entityId, key), "Metadata entry retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a metadata entry's value")
    public ResponseEntity<ApiResponse<MetadataKvResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateMetadataKvRequest request) {
        return ApiResponse.ok(metadataKvService.update(id, request), "Metadata entry updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a metadata entry")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        metadataKvService.delete(id);
        return ApiResponse.noContent();
    }
}
