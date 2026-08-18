package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLoanDisbursementRequest(
        @NotNull
        UUID loanApplicationId,

        @NotNull
        UUID loanContractId,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String disbursementMethod,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String destinationAccount
) {
}
