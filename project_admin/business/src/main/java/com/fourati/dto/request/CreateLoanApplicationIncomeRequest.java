package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanApplicationIncomeRequest(
        @NotNull
        UUID loanApplicationId,

        @NotBlank
        @Size(max = 30)
        @SafeInput
        String incomeType,

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal monthlyAmount,

        @Size(max = 200)
        @SafeInput
        String source
) {
}
