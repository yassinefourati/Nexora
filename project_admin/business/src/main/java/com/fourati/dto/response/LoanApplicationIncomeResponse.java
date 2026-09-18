package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApplicationIncomeResponse(
        UUID id,
        UUID loanApplicationId,
        String incomeType,
        BigDecimal monthlyAmount,
        String source,
        Instant createdAt,
        Instant updatedAt
) {
}
