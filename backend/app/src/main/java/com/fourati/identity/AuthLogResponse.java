package com.fourati.identity;

import java.time.Instant;
import java.util.UUID;

public record AuthLogResponse(
        UUID id,
        UUID userId,
        String eventType,
        String ipAddress,
        String userAgent,
        String metadata,
        Instant createdAt,
        Long version
) {
}
