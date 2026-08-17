package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.DocumentReviewResponse;
import com.fourati.service.DocumentReviewService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/document-reviews")
@Tag(name = "Document Reviews", description = "Read-only audit trail of document review decisions.")
public class DocumentReviewController {

    private final DocumentReviewService documentReviewService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List review history for a document")
    public ResponseEntity<ApiResponse<List<DocumentReviewResponse>>> listByDocument(
            @RequestParam UUID documentId) {
        return ApiResponse.ok(documentReviewService.findByDocumentId(documentId), "Document reviews retrieved");
    }
}
