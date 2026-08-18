package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.LoanOfferStatusHistoryResponse;
import com.fourati.service.LoanOfferStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-offer-status-history")
@Tag(name = "Loan Offer Status History", description = "Read-only audit trail of loan offer status transitions.")
public class LoanOfferStatusHistoryController {

    private final LoanOfferStatusHistoryService loanOfferStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a loan offer")
    public ResponseEntity<ApiResponse<List<LoanOfferStatusHistoryResponse>>> listByOffer(
            @RequestParam UUID loanOfferId) {
        return ApiResponse.ok(loanOfferStatusHistoryService.findByLoanOfferId(loanOfferId), "Loan offer status history retrieved");
    }
}
