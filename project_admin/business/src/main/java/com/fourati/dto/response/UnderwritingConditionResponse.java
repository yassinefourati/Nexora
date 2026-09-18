package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UnderwritingConditionResponse(
        UUID id,
        UUID underwritingCaseId,
        String description,
        String status,
        Instant satisfiedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
