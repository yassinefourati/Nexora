package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanContractResponse(
        UUID id,
        UUID loanApplicationId,
        UUID loanOfferId,
        String contractNumber,
        String status,
        BigDecimal principalAmount,
        int termMonths,
        BigDecimal interestRate,
        String documentUrl,
        Instant finalizedAt,
        Instant cancelledAt,
        String cancellationReason,
        Instant createdAt,
        Instant updatedAt
) {
}
