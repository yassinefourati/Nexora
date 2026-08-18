package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DecideUnderwritingCaseRequest(
        @NotBlank
        @Size(max = 30)
        @SafeInput
        String decision,

        @Size(max = 1000)
        @SafeInput
        String decisionReason,

        @DecimalMin(value = "0", inclusive = false)
        BigDecimal approvedAmount,

        @Min(1)
        Integer approvedTermMonths
) {
}
