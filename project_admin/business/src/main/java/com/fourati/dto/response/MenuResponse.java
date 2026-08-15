package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MenuResponse(
        UUID id,
        String name,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
