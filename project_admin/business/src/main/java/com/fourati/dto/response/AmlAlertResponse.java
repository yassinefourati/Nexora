package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AmlAlertResponse(
        UUID id,
        UUID amlScreeningId,
        String alertType,
        String severity,
        String status,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
