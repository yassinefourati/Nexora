package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateDocumentRequest;
import com.fourati.dto.request.ReviewDocumentRequest;
import com.fourati.dto.response.DocumentResponse;
import com.fourati.service.DocumentService;
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
@RequestMapping(ApiConstants.VERSION + "/documents")
@Tag(name = "Documents", description = "Manage document metadata and object-storage references. File bytes live in external object storage.")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a new uploaded document")
    public ResponseEntity<ApiResponse<DocumentResponse>> create(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentResponse created = documentService.create(request);
        return ApiResponse.created(created, "Document registered successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a document by id")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(documentService.findById(id), "Document retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List documents (paginated)")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(documentService.findAll(pageable), "Documents retrieved");
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Review a document (verify or reject)")
    public ResponseEntity<ApiResponse<DocumentResponse>> review(@PathVariable UUID id,
            @Valid @RequestBody ReviewDocumentRequest request) {
        return ApiResponse.ok(documentService.review(id, request), "Document reviewed");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ApiResponse.noContent();
    }
}
