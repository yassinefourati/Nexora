package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanAccountStatusHistoryResponse;
import com.fourati.service.LoanAccountStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-account-status-history")
@Tag(name = "Loan Account Status History", description = "Read-only audit trail of loan account status transitions.")
public class LoanAccountStatusHistoryController {

    private final LoanAccountStatusHistoryService loanAccountStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan account")
    public ResponseEntity<ApiResponse<List<LoanAccountStatusHistoryResponse>>> listByAccount(
            @RequestParam UUID loanAccountId) {
        return ApiResponse.ok(loanAccountStatusHistoryService.findByLoanAccountId(loanAccountId), "Loan account status history retrieved");
    }
}
