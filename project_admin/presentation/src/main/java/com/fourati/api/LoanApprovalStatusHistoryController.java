package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanApprovalStatusHistoryResponse;
import com.fourati.service.LoanApprovalStatusHistoryService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/loan-approval-status-history")
@Tag(name = "Loan Approval Status History", description = "Read-only audit trail of loan approval status transitions.")
public class LoanApprovalStatusHistoryController {

    private final LoanApprovalStatusHistoryService loanApprovalStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan approval")
    public ResponseEntity<ApiResponse<List<LoanApprovalStatusHistoryResponse>>> listByApproval(
            @RequestParam UUID loanApprovalId) {
        return ApiResponse.ok(loanApprovalStatusHistoryService.findByLoanApprovalId(loanApprovalId), "Loan approval status history retrieved");
    }
}
