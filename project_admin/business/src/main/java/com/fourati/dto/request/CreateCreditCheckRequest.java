package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCreditCheckRequest(
        @NotNull
        UUID loanApplicationId,

        @NotNull
        UUID customerId
) {
}
