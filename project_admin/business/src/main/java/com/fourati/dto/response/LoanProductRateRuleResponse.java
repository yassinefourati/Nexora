package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanProductRateRuleResponse(
        UUID id,
        UUID loanProductId,
        String rateType,
        BigDecimal baseRate,
        BigDecimal margin,
        Instant createdAt,
        Instant updatedAt
) {
}
