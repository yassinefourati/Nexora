package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean system,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
