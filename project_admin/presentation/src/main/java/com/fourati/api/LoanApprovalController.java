package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.RejectLoanApprovalRequest;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.service.LoanApprovalService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-approvals")
@Tag(name = "Loan Approvals", description = "Formalize underwriting decisions into approved loan terms.")
public class LoanApprovalController {

    private final LoanApprovalService loanApprovalService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new loan approval record")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> create(@Valid @RequestBody CreateLoanApprovalRequest request) {
        LoanApprovalResponse created = loanApprovalService.create(request);
        return ApiResponse.created(created, "Loan approval opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan approval by id")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanApprovalService.findById(id), "Loan approval retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan approvals (paginated)")
    public ResponseEntity<ApiResponse<List<LoanApprovalResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanApprovalService.findAll(pageable), "Loan approvals retrieved");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a loan and record its final terms")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> approve(@PathVariable UUID id,
            @Valid @RequestBody ApproveLoanApprovalRequest request) {
        return ApiResponse.ok(loanApprovalService.approve(id, request), "Loan approved");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a loan approval")
    public ResponseEntity<ApiResponse<LoanApprovalResponse>> reject(@PathVariable UUID id,
            @Valid @RequestBody RejectLoanApprovalRequest request) {
        return ApiResponse.ok(loanApprovalService.reject(id, request), "Loan approval rejected");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan approval")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApprovalService.delete(id);
        return ApiResponse.noContent();
    }
}
