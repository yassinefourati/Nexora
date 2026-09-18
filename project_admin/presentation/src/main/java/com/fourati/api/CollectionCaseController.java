package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCollectionCaseRequest;
import com.fourati.dto.request.EscalateCollectionCaseRequest;
import com.fourati.dto.request.ResolveCollectionCaseRequest;
import com.fourati.dto.request.WriteOffCollectionCaseRequest;
import com.fourati.dto.response.CollectionCaseResponse;
import com.fourati.service.CollectionCaseService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/collection-cases")
@Tag(name = "Collection Cases", description = "Track collection efforts on overdue loan installments.")
public class CollectionCaseController {

    private final CollectionCaseService collectionCaseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new collection case for an overdue installment")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> create(@Valid @RequestBody CreateCollectionCaseRequest request) {
        CollectionCaseResponse created = collectionCaseService.create(request);
        return ApiResponse.created(created, "Collection case opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a collection case by id")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(collectionCaseService.findById(id), "Collection case retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List collection cases (paginated)")
    public ResponseEntity<ApiResponse<List<CollectionCaseResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(collectionCaseService.findAll(pageable), "Collection cases retrieved");
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Escalate a collection case to the next stage")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> escalate(@PathVariable UUID id,
            @Valid @RequestBody EscalateCollectionCaseRequest request) {
        return ApiResponse.ok(collectionCaseService.escalate(id, request), "Collection case escalated");
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve a collection case")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> resolve(@PathVariable UUID id,
            @Valid @RequestBody ResolveCollectionCaseRequest request) {
        return ApiResponse.ok(collectionCaseService.resolve(id, request), "Collection case resolved");
    }

    @PostMapping("/{id}/write-off")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Write off a collection case")
    public ResponseEntity<ApiResponse<CollectionCaseResponse>> writeOff(@PathVariable UUID id,
            @Valid @RequestBody WriteOffCollectionCaseRequest request) {
        return ApiResponse.ok(collectionCaseService.writeOff(id, request), "Collection case written off");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a collection case")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        collectionCaseService.delete(id);
        return ApiResponse.noContent();
    }
}
