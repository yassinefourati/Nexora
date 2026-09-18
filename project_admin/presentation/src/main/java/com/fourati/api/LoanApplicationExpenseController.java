package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanApplicationExpenseRequest;
import com.fourati.dto.response.LoanApplicationExpenseResponse;
import com.fourati.service.LoanApplicationExpenseService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-application-expenses")
@Tag(name = "Loan Application Expenses", description = "Manage declared monthly expenses on a loan application.")
public class LoanApplicationExpenseController {

    private final LoanApplicationExpenseService loanApplicationExpenseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an expense to a loan application")
    public ResponseEntity<ApiResponse<LoanApplicationExpenseResponse>> create(
            @Valid @RequestBody CreateLoanApplicationExpenseRequest request) {
        LoanApplicationExpenseResponse created = loanApplicationExpenseService.create(request);
        return ApiResponse.created(created, "Loan application expense added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List expenses for a loan application")
    public ResponseEntity<ApiResponse<List<LoanApplicationExpenseResponse>>> listByLoanApplication(
            @RequestParam UUID loanApplicationId) {
        return ApiResponse.ok(loanApplicationExpenseService.findByLoanApplicationId(loanApplicationId), "Loan application expenses retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an expense from a loan application")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApplicationExpenseService.delete(id);
        return ApiResponse.noContent();
    }
}
