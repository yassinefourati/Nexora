package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CompleteLoanDisbursementRequest;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.request.FailLoanDisbursementRequest;
import com.fourati.dto.response.LoanDisbursementResponse;
import com.fourati.service.LoanDisbursementService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-disbursements")
@Tag(name = "Loan Disbursements", description = "Release principal funds for a loan application once its contract is fully signed.")
public class LoanDisbursementController {

    private final LoanDisbursementService loanDisbursementService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new loan disbursement")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> create(@Valid @RequestBody CreateLoanDisbursementRequest request) {
        LoanDisbursementResponse created = loanDisbursementService.create(request);
        return ApiResponse.created(created, "Loan disbursement created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan disbursement by id")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanDisbursementService.findById(id), "Loan disbursement retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan disbursements (paginated)")
    public ResponseEntity<ApiResponse<List<LoanDisbursementResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanDisbursementService.findAll(pageable), "Loan disbursements retrieved");
    }

    @PostMapping("/{id}/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initiate a loan disbursement")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> initiate(@PathVariable UUID id) {
        return ApiResponse.ok(loanDisbursementService.initiate(id), "Loan disbursement initiated");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete a loan disbursement")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> complete(@PathVariable UUID id,
            @Valid @RequestBody CompleteLoanDisbursementRequest request) {
        return ApiResponse.ok(loanDisbursementService.complete(id, request), "Loan disbursement completed");
    }

    @PostMapping("/{id}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a loan disbursement as failed")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> fail(@PathVariable UUID id,
            @Valid @RequestBody FailLoanDisbursementRequest request) {
        return ApiResponse.ok(loanDisbursementService.fail(id, request), "Loan disbursement marked as failed");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan disbursement")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanDisbursementService.delete(id);
        return ApiResponse.noContent();
    }
}
