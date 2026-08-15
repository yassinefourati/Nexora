package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MetadataKvResponse(
        UUID id,
        String entityType,
        UUID entityId,
        String key,
        String value,
        Instant createdAt,
        Instant updatedAt
) {
}
