package com.fourati.audit;

import java.time.Instant;
import java.util.UUID;

public record SystemEventResponse(
        UUID id,
        String eventType,
        String severity,
        String source,
        String payload,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
