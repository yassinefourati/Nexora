package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateFraudCheckRequest(
        @NotNull
        UUID loanApplicationId
) {
}
