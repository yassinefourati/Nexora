package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanApprovalConditionRequest;
import com.fourati.dto.response.LoanApprovalConditionResponse;
import com.fourati.service.LoanApprovalConditionService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-approval-conditions")
@Tag(name = "Loan Approval Conditions", description = "Manage conditions attached to a loan approval.")
public class LoanApprovalConditionController {

    private final LoanApprovalConditionService loanApprovalConditionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Attach a condition to a loan approval")
    public ResponseEntity<ApiResponse<LoanApprovalConditionResponse>> create(
            @Valid @RequestBody CreateLoanApprovalConditionRequest request) {
        LoanApprovalConditionResponse created = loanApprovalConditionService.create(request);
        return ApiResponse.created(created, "Loan approval condition added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List conditions for a loan approval")
    public ResponseEntity<ApiResponse<List<LoanApprovalConditionResponse>>> listByApproval(
            @RequestParam UUID loanApprovalId) {
        return ApiResponse.ok(loanApprovalConditionService.findByLoanApprovalId(loanApprovalId), "Loan approval conditions retrieved");
    }

    @PostMapping("/{id}/satisfy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark a loan approval condition as satisfied")
    public ResponseEntity<ApiResponse<LoanApprovalConditionResponse>> satisfy(@PathVariable UUID id) {
        return ApiResponse.ok(loanApprovalConditionService.satisfy(id), "Loan approval condition satisfied");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a condition from a loan approval")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApprovalConditionService.delete(id);
        return ApiResponse.noContent();
    }
}
