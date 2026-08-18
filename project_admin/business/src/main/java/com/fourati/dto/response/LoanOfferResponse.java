package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanOfferResponse(
        UUID id,
        UUID loanApplicationId,
        UUID loanApprovalId,
        String status,
        BigDecimal offeredAmount,
        int offeredTermMonths,
        BigDecimal interestRate,
        String declineReason,
        Instant issuedAt,
        Instant expiresAt,
        Instant acceptedAt,
        Instant declinedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
