package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FraudAlertResponse(
        UUID id,
        UUID fraudCheckId,
        String indicatorType,
        String severity,
        String status,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
