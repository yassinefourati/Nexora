package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanApprovalConditionResponse(
        UUID id,
        UUID loanApprovalId,
        String description,
        String status,
        Instant satisfiedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
