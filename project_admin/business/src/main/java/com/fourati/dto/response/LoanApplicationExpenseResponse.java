package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApplicationExpenseResponse(
        UUID id,
        UUID loanApplicationId,
        String expenseType,
        BigDecimal monthlyAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
