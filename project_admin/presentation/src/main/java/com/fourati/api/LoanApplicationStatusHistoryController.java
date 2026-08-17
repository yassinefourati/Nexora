package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanApplicationStatusHistoryResponse;
import com.fourati.service.LoanApplicationStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-application-status-history")
@Tag(name = "Loan Application Status History", description = "Read-only audit trail of loan application status transitions.")
public class LoanApplicationStatusHistoryController {

    private final LoanApplicationStatusHistoryService loanApplicationStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan application")
    public ResponseEntity<ApiResponse<List<LoanApplicationStatusHistoryResponse>>> listByLoanApplication(
            @RequestParam UUID loanApplicationId) {
        return ApiResponse.ok(loanApplicationStatusHistoryService.findByLoanApplicationId(loanApplicationId), "Loan application status history retrieved");
    }
}
