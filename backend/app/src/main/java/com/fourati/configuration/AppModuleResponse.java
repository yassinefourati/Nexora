package com.fourati.configuration;

import java.time.Instant;
import java.util.UUID;

public record AppModuleResponse(
        UUID id,
        String key,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
