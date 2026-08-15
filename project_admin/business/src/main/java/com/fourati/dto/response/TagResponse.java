package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(
        UUID id,
        String name,
        String color,
        Instant createdAt,
        Instant updatedAt
) {
}
