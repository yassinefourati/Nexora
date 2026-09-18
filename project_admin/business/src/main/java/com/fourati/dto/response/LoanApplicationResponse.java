package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApplicationResponse(
        UUID id,
        UUID customerId,
        UUID loanProductId,
        String status,
        BigDecimal requestedAmount,
        int requestedTermMonths,
        String purpose,
        Instant submittedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
