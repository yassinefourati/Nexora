package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record MenuResponse(
        UUID id,
        String name,
        String code,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
