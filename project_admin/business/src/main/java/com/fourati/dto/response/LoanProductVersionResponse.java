package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanProductVersionResponse(
        UUID id,
        UUID loanProductId,
        int versionNumber,
        String status,
        Instant effectiveFrom,
        Instant effectiveTo,
        Instant createdAt,
        Instant updatedAt
) {
}
