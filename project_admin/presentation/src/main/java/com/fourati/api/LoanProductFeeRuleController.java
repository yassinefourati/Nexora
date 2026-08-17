package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanProductFeeRuleRequest;
import com.fourati.dto.response.LoanProductFeeRuleResponse;
import com.fourati.service.LoanProductFeeRuleService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-product-fee-rules")
@Tag(name = "Loan Product Fee Rules", description = "Manage fee rules for a loan product.")
public class LoanProductFeeRuleController {

    private final LoanProductFeeRuleService loanProductFeeRuleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a fee rule to a loan product")
    public ResponseEntity<ApiResponse<LoanProductFeeRuleResponse>> create(
            @Valid @RequestBody CreateLoanProductFeeRuleRequest request) {
        LoanProductFeeRuleResponse created = loanProductFeeRuleService.create(request);
        return ApiResponse.created(created, "Loan product fee rule added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List fee rules for a loan product")
    public ResponseEntity<ApiResponse<List<LoanProductFeeRuleResponse>>> listByLoanProduct(
            @RequestParam UUID loanProductId) {
        return ApiResponse.ok(loanProductFeeRuleService.findByLoanProductId(loanProductId), "Loan product fee rules retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a fee rule from a loan product")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanProductFeeRuleService.delete(id);
        return ApiResponse.noContent();
    }
}
