package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanProductFeeRuleRequest(
        @NotNull
        UUID loanProductId,

        @NotBlank
        @Size(max = 30)
        @SafeInput
        String feeType,

        @DecimalMin(value = "0", inclusive = true)
        BigDecimal feeAmount,

        @DecimalMin(value = "0", inclusive = true)
        BigDecimal feePercentage,

        boolean mandatory
) {
}
