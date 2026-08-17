package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanProductFeeRuleResponse(
        UUID id,
        UUID loanProductId,
        String feeType,
        BigDecimal feeAmount,
        BigDecimal feePercentage,
        boolean mandatory,
        Instant createdAt,
        Instant updatedAt
) {
}
