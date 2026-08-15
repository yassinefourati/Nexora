package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String resource,
        String action,
        String code,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
