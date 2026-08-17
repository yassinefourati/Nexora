package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.UpdateLoanProductRequest;
import com.fourati.dto.response.LoanProductResponse;
import com.fourati.service.LoanProductService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-products")
@Tag(name = "Loan Products", description = "Manage the configurable catalog of loan products offered by the platform.")
public class LoanProductController {

    private final LoanProductService loanProductService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new loan product")
    public ResponseEntity<ApiResponse<LoanProductResponse>> create(@Valid @RequestBody CreateLoanProductRequest request) {
        LoanProductResponse created = loanProductService.create(request);
        return ApiResponse.created(created, "Loan product created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan product by id")
    public ResponseEntity<ApiResponse<LoanProductResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanProductService.findById(id), "Loan product retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan products (paginated)")
    public ResponseEntity<ApiResponse<List<LoanProductResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanProductService.findAll(pageable), "Loan products retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a loan product")
    public ResponseEntity<ApiResponse<LoanProductResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateLoanProductRequest request) {
        return ApiResponse.ok(loanProductService.update(id, request), "Loan product updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanProductService.delete(id);
        return ApiResponse.noContent();
    }
}
