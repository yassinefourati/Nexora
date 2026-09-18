package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanRepaymentStatusHistoryResponse;
import com.fourati.service.LoanRepaymentStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-repayment-status-history")
@Tag(name = "Loan Repayment Status History", description = "Read-only audit trail of loan repayment status transitions.")
public class LoanRepaymentStatusHistoryController {

    private final LoanRepaymentStatusHistoryService loanRepaymentStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan repayment")
    public ResponseEntity<ApiResponse<List<LoanRepaymentStatusHistoryResponse>>> listByRepayment(
            @RequestParam UUID loanRepaymentId) {
        return ApiResponse.ok(loanRepaymentStatusHistoryService.findByLoanRepaymentId(loanRepaymentId), "Loan repayment status history retrieved");
    }
}
