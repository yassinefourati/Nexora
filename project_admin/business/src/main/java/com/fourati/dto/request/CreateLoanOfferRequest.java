package com.fourati.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateLoanOfferRequest(
        @NotNull
        UUID loanApplicationId,

        @NotNull
        UUID loanApprovalId,

        @NotNull
        @Future
        Instant expiresAt
) {
}
