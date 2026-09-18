package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateLoanContractRequest(
        @NotNull
        UUID loanApplicationId,

        @NotNull
        UUID loanOfferId,

        @NotBlank
        @Size(max = 50)
        @SafeInput
        String contractNumber
) {
}
