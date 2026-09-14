package com.fourati.organization;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        UUID organizationId,
        UUID parentDepartmentId,
        String name,
        String code,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
