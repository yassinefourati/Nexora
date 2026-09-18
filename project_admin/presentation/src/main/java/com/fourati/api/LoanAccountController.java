package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CloseLoanAccountRequest;
import com.fourati.dto.request.CreateLoanAccountRequest;
import com.fourati.dto.request.DefaultLoanAccountRequest;
import com.fourati.dto.response.LoanAccountResponse;
import com.fourati.service.LoanAccountService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-accounts")
@Tag(name = "Loan Accounts", description = "Servicing accounts opened once a loan disbursement completes.")
public class LoanAccountController {

    private final LoanAccountService loanAccountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new loan account and generate its repayment schedule")
    public ResponseEntity<ApiResponse<LoanAccountResponse>> create(@Valid @RequestBody CreateLoanAccountRequest request) {
        LoanAccountResponse created = loanAccountService.create(request);
        return ApiResponse.created(created, "Loan account opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan account by id")
    public ResponseEntity<ApiResponse<LoanAccountResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanAccountService.findById(id), "Loan account retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan accounts (paginated)")
    public ResponseEntity<ApiResponse<List<LoanAccountResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanAccountService.findAll(pageable), "Loan accounts retrieved");
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close a loan account")
    public ResponseEntity<ApiResponse<LoanAccountResponse>> close(@PathVariable UUID id,
            @Valid @RequestBody CloseLoanAccountRequest request) {
        return ApiResponse.ok(loanAccountService.close(id, request), "Loan account closed");
    }

    @PostMapping("/{id}/default")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a loan account as defaulted")
    public ResponseEntity<ApiResponse<LoanAccountResponse>> markDefaulted(@PathVariable UUID id,
            @Valid @RequestBody DefaultLoanAccountRequest request) {
        return ApiResponse.ok(loanAccountService.markDefaulted(id, request), "Loan account marked as defaulted");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan account")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanAccountService.delete(id);
        return ApiResponse.noContent();
    }
}
