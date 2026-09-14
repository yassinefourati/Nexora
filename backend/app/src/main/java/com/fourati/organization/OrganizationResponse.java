package com.fourati.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        UUID parentOrganizationId,
        String name,
        String code,
        OrganizationStatus status,
        String metadata,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
