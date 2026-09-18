package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanContractStatusHistoryResponse;
import com.fourati.service.LoanContractStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-contract-status-history")
@Tag(name = "Loan Contract Status History", description = "Read-only audit trail of loan contract status transitions.")
public class LoanContractStatusHistoryController {

    private final LoanContractStatusHistoryService loanContractStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan contract")
    public ResponseEntity<ApiResponse<List<LoanContractStatusHistoryResponse>>> listByContract(
            @RequestParam UUID loanContractId) {
        return ApiResponse.ok(loanContractStatusHistoryService.findByLoanContractId(loanContractId), "Loan contract status history retrieved");
    }
}
