package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        UUID parentOrganizationId,
        String name,
        String code,
        String status,
        String metadata,
        Instant createdAt,
        Instant updatedAt
) {
}
