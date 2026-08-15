package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        UUID organizationId,
        UUID departmentId,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
}
