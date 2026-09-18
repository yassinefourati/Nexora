package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanApplicationRequest(
        @NotNull
        UUID customerId,

        @NotNull
        UUID loanProductId,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal requestedAmount,

        @NotNull
        @Min(1)
        Integer requestedTermMonths,

        @Size(max = 200)
        @SafeInput
        String purpose
) {
}
