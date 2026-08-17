package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanProductEligibilityRuleRequest;
import com.fourati.dto.response.LoanProductEligibilityRuleResponse;
import com.fourati.service.LoanProductEligibilityRuleService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-product-eligibility-rules")
@Tag(name = "Loan Product Eligibility Rules", description = "Manage the minimum eligibility thresholds for a loan product.")
public class LoanProductEligibilityRuleController {

    private final LoanProductEligibilityRuleService loanProductEligibilityRuleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an eligibility rule to a loan product")
    public ResponseEntity<ApiResponse<LoanProductEligibilityRuleResponse>> create(
            @Valid @RequestBody CreateLoanProductEligibilityRuleRequest request) {
        LoanProductEligibilityRuleResponse created = loanProductEligibilityRuleService.create(request);
        return ApiResponse.created(created, "Loan product eligibility rule added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List eligibility rules for a loan product")
    public ResponseEntity<ApiResponse<List<LoanProductEligibilityRuleResponse>>> listByLoanProduct(
            @RequestParam UUID loanProductId) {
        return ApiResponse.ok(loanProductEligibilityRuleService.findByLoanProductId(loanProductId), "Loan product eligibility rules retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an eligibility rule from a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanProductEligibilityRuleService.delete(id);
        return ApiResponse.noContent();
    }
}
