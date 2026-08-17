package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateDocumentRequirementRequest;
import com.fourati.dto.response.DocumentRequirementResponse;
import com.fourati.service.DocumentRequirementService;
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
@RequestMapping(ApiConstants.VERSION + "/document-requirements")
@Tag(name = "Document Requirements", description = "Manage the configurable per-loan-product document checklist.")
public class DocumentRequirementController {

    private final DocumentRequirementService documentRequirementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a document requirement to a loan product")
    public ResponseEntity<ApiResponse<DocumentRequirementResponse>> create(
            @Valid @RequestBody CreateDocumentRequirementRequest request) {
        DocumentRequirementResponse created = documentRequirementService.create(request);
        return ApiResponse.created(created, "Document requirement added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List document requirements for a loan product")
    public ResponseEntity<ApiResponse<List<DocumentRequirementResponse>>> listByLoanProduct(
            @RequestParam UUID loanProductId) {
        return ApiResponse.ok(documentRequirementService.findByLoanProductId(loanProductId), "Document requirements retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a document requirement from a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentRequirementService.delete(id);
        return ApiResponse.noContent();
    }
}
