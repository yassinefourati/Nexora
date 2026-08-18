package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanApplicationIncomeRequest;
import com.fourati.dto.response.LoanApplicationIncomeResponse;
import com.fourati.service.LoanApplicationIncomeService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-application-incomes")
@Tag(name = "Loan Application Incomes", description = "Manage declared income sources on a loan application.")
public class LoanApplicationIncomeController {

    private final LoanApplicationIncomeService loanApplicationIncomeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an income source to a loan application")
    public ResponseEntity<ApiResponse<LoanApplicationIncomeResponse>> create(
            @Valid @RequestBody CreateLoanApplicationIncomeRequest request) {
        LoanApplicationIncomeResponse created = loanApplicationIncomeService.create(request);
        return ApiResponse.created(created, "Loan application income added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List income sources for a loan application")
    public ResponseEntity<ApiResponse<List<LoanApplicationIncomeResponse>>> listByLoanApplication(
            @RequestParam UUID loanApplicationId) {
        return ApiResponse.ok(loanApplicationIncomeService.findByLoanApplicationId(loanApplicationId), "Loan application incomes retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an income source from a loan application")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApplicationIncomeService.delete(id);
        return ApiResponse.noContent();
    }
}
