package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanProductEligibilityRuleResponse(
        UUID id,
        UUID loanProductId,
        Integer minCreditScore,
        BigDecimal minMonthlyIncome,
        BigDecimal maxDebtToIncomeRatio,
        Integer minAge,
        Instant createdAt,
        Instant updatedAt
) {
}
