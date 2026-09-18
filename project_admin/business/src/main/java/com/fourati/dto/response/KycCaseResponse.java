package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KycCaseResponse(
        UUID id,
        UUID customerId,
        String status,
        Instant initiatedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
