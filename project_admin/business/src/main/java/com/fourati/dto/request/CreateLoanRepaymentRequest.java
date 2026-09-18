package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateLoanRepaymentRequest(
        @NotNull
        UUID loanAccountId,

        @NotNull
        UUID loanInstallmentId,

        @NotNull
        @DecimalMin(value = "0", inclusive = false)
        BigDecimal amount,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String paymentMethod
) {
}
