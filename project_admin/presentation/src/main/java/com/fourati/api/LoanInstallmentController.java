package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanInstallmentResponse;
import com.fourati.service.LoanInstallmentService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-installments")
@Tag(name = "Loan Installments", description = "Read-only repayment schedule for a loan account.")
public class LoanInstallmentController {

    private final LoanInstallmentService loanInstallmentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List installments for a loan account")
    public ResponseEntity<ApiResponse<List<LoanInstallmentResponse>>> listByAccount(
            @RequestParam UUID loanAccountId) {
        return ApiResponse.ok(loanInstallmentService.findByLoanAccountId(loanAccountId), "Loan installments retrieved");
    }
}
