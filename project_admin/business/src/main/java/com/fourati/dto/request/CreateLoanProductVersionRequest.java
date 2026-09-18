package com.fourati.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateLoanProductVersionRequest(
        @NotNull
        UUID loanProductId,

        @NotNull
        @Min(1)
        Integer versionNumber,

        Instant effectiveFrom,

        Instant effectiveTo
) {
}
