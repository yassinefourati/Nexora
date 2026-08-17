package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateApplicationDocumentRequest;
import com.fourati.dto.response.ApplicationDocumentResponse;
import com.fourati.service.ApplicationDocumentService;
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
@RequestMapping(ApiConstants.VERSION + "/application-documents")
@Tag(name = "Application Documents", description = "Manage documents attached to a loan application.")
public class ApplicationDocumentController {

    private final ApplicationDocumentService applicationDocumentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Attach a document to a loan application")
    public ResponseEntity<ApiResponse<ApplicationDocumentResponse>> create(
            @Valid @RequestBody CreateApplicationDocumentRequest request) {
        ApplicationDocumentResponse created = applicationDocumentService.create(request);
        return ApiResponse.created(created, "Document attached to loan application successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List documents attached to a loan application")
    public ResponseEntity<ApiResponse<List<ApplicationDocumentResponse>>> listByLoanApplication(
            @RequestParam UUID loanApplicationId) {
        return ApiResponse.ok(applicationDocumentService.findByLoanApplicationId(loanApplicationId), "Application documents retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detach a document from a loan application")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        applicationDocumentService.delete(id);
        return ApiResponse.noContent();
    }
}
