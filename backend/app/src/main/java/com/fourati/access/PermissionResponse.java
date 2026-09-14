package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String resource,
        String action,
        String code,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long version
) implements java.io.Serializable {
}
