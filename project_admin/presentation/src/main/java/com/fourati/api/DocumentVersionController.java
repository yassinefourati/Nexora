package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateDocumentVersionRequest;
import com.fourati.dto.response.DocumentVersionResponse;
import com.fourati.service.DocumentVersionService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/document-versions")
@Tag(name = "Document Versions", description = "Manage prior versions of a document.")
public class DocumentVersionController {

    private final DocumentVersionService documentVersionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a version to a document")
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> create(
            @Valid @RequestBody CreateDocumentVersionRequest request) {
        DocumentVersionResponse created = documentVersionService.create(request);
        return ApiResponse.created(created, "Document version added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List versions for a document")
    public ResponseEntity<ApiResponse<List<DocumentVersionResponse>>> listByDocument(
            @RequestParam UUID documentId) {
        return ApiResponse.ok(documentVersionService.findByDocumentId(documentId), "Document versions retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a version from a document")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentVersionService.delete(id);
        return ApiResponse.noContent();
    }
}
