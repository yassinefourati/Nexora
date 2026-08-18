package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CompleteLoanRepaymentRequest;
import com.fourati.dto.request.CreateLoanRepaymentRequest;
import com.fourati.dto.request.FailLoanRepaymentRequest;
import com.fourati.dto.response.LoanRepaymentResponse;
import com.fourati.service.LoanRepaymentService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(ApiConstants.VERSION + "/loan-repayments")
@Tag(name = "Loan Repayments", description = "Customer payments captured against a loan account's installment schedule.")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a new loan repayment")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> create(@Valid @RequestBody CreateLoanRepaymentRequest request) {
        LoanRepaymentResponse created = loanRepaymentService.create(request);
        return ApiResponse.created(created, "Loan repayment recorded successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan repayment by id")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanRepaymentService.findById(id), "Loan repayment retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan repayments, optionally filtered by loan account (paginated)")
    public ResponseEntity<ApiResponse<List<LoanRepaymentResponse>>> list(
            @RequestParam(required = false) UUID loanAccountId, Pageable pageable) {
        if (loanAccountId != null) {
            return ApiResponse.ok(loanRepaymentService.findByLoanAccountId(loanAccountId), "Loan repayments retrieved");
        }
        return ApiResponse.paged(loanRepaymentService.findAll(pageable), "Loan repayments retrieved");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete a loan repayment")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> complete(@PathVariable UUID id,
            @Valid @RequestBody CompleteLoanRepaymentRequest request) {
        return ApiResponse.ok(loanRepaymentService.complete(id, request), "Loan repayment completed");
    }

    @PostMapping("/{id}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a loan repayment as failed")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> fail(@PathVariable UUID id,
            @Valid @RequestBody FailLoanRepaymentRequest request) {
        return ApiResponse.ok(loanRepaymentService.fail(id, request), "Loan repayment marked as failed");
    }
}
