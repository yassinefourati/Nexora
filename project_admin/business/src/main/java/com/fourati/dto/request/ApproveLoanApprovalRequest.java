package com.fourati.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fourati.platform.security.validation.SafeInput;

import java.math.BigDecimal;

public record ApproveLoanApprovalRequest(
        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal approvedAmount,

        @NotNull
        @Min(1)
        Integer approvedTermMonths,

        @NotNull
        @DecimalMin(value = "0")
        BigDecimal interestRate,

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String approvedBy
) {
}
