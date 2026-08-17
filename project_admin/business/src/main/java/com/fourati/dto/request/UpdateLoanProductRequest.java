package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateLoanProductRequest(
        @NotBlank
        @Size(max = 50)
        @SafeInput
        String code,

        @NotBlank
        @Size(max = 200)
        @SafeInput
        String name,

        @NotBlank
        @Size(max = 30)
        @SafeInput
        String productType,

        @Size(max = 20)
        @SafeInput
        String status,

        @Size(max = 3)
        @SafeInput
        String currency,

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal minAmount,

        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal maxAmount,

        @NotNull
        @Min(1)
        Integer minTermMonths,

        @NotNull
        @Min(1)
        Integer maxTermMonths,

        @SafeInput
        String description
) {
}
