package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AppModuleResponse(
        UUID id,
        String key,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
