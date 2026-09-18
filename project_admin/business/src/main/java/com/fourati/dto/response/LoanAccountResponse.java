package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanAccountResponse(
        UUID id,
        UUID loanApplicationId,
        UUID loanDisbursementId,
        String accountNumber,
        String status,
        BigDecimal principalAmount,
        BigDecimal interestRate,
        int termMonths,
        BigDecimal outstandingPrincipal,
        Instant openedAt,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
