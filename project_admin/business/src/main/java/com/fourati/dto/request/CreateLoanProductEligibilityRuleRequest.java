package com.fourati.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanProductEligibilityRuleRequest(
        @NotNull
        UUID loanProductId,

        @Min(0)
        Integer minCreditScore,

        @DecimalMin(value = "0", inclusive = true)
        BigDecimal minMonthlyIncome,

        @DecimalMin(value = "0", inclusive = true)
        @DecimalMax(value = "1", inclusive = true)
        BigDecimal maxDebtToIncomeRatio,

        @Min(0)
        Integer minAge
) {
}
