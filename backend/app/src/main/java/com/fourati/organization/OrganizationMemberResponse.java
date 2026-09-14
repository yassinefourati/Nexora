package com.fourati.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationMemberResponse(
        UUID id,
        UUID organizationId,
        UUID userId,
        UUID departmentId,
        UUID teamId,
        String title,
        boolean primary,
        Instant joinedAt,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
