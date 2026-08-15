package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record EntityTagResponse(
        UUID id,
        UUID tagId,
        String entityType,
        UUID entityId,
        Instant createdAt,
        Instant updatedAt
) {
}
