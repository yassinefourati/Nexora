package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserRoleResponse(
        UUID id,
        UUID userId,
        UUID roleId,
        UUID organizationId,
        Instant expiresAt,
        Instant assignedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
