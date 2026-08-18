package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanDisbursementStatusHistoryResponse;
import com.fourati.service.LoanDisbursementStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-disbursement-status-history")
@Tag(name = "Loan Disbursement Status History", description = "Read-only audit trail of loan disbursement status transitions.")
public class LoanDisbursementStatusHistoryController {

    private final LoanDisbursementStatusHistoryService loanDisbursementStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan disbursement")
    public ResponseEntity<ApiResponse<List<LoanDisbursementStatusHistoryResponse>>> listByDisbursement(
            @RequestParam UUID loanDisbursementId) {
        return ApiResponse.ok(loanDisbursementStatusHistoryService.findByLoanDisbursementId(loanDisbursementId), "Loan disbursement status history retrieved");
    }
}
