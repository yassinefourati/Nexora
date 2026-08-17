package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanProductRateRuleRequest;
import com.fourati.dto.response.LoanProductRateRuleResponse;
import com.fourati.service.LoanProductRateRuleService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-product-rate-rules")
@Tag(name = "Loan Product Rate Rules", description = "Manage interest rate rules for a loan product.")
public class LoanProductRateRuleController {

    private final LoanProductRateRuleService loanProductRateRuleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a rate rule to a loan product")
    public ResponseEntity<ApiResponse<LoanProductRateRuleResponse>> create(
            @Valid @RequestBody CreateLoanProductRateRuleRequest request) {
        LoanProductRateRuleResponse created = loanProductRateRuleService.create(request);
        return ApiResponse.created(created, "Loan product rate rule added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List rate rules for a loan product")
    public ResponseEntity<ApiResponse<List<LoanProductRateRuleResponse>>> listByLoanProduct(
            @RequestParam UUID loanProductId) {
        return ApiResponse.ok(loanProductRateRuleService.findByLoanProductId(loanProductId), "Loan product rate rules retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a rate rule from a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanProductRateRuleService.delete(id);
        return ApiResponse.noContent();
    }
}
