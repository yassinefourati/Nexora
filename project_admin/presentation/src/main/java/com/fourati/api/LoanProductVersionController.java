package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanProductVersionRequest;
import com.fourati.dto.response.LoanProductVersionResponse;
import com.fourati.service.LoanProductVersionService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-product-versions")
@Tag(name = "Loan Product Versions", description = "Manage versioned snapshots of a loan product's terms.")
public class LoanProductVersionController {

    private final LoanProductVersionService loanProductVersionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a version to a loan product")
    public ResponseEntity<ApiResponse<LoanProductVersionResponse>> create(
            @Valid @RequestBody CreateLoanProductVersionRequest request) {
        LoanProductVersionResponse created = loanProductVersionService.create(request);
        return ApiResponse.created(created, "Loan product version added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List versions for a loan product")
    public ResponseEntity<ApiResponse<List<LoanProductVersionResponse>>> listByLoanProduct(
            @RequestParam UUID loanProductId) {
        return ApiResponse.ok(loanProductVersionService.findByLoanProductId(loanProductId), "Loan product versions retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a version from a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanProductVersionService.delete(id);
        return ApiResponse.noContent();
    }
}
