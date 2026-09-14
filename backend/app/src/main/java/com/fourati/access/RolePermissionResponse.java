package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionResponse(
        UUID id,
        UUID roleId,
        UUID permissionId,
        String conditions,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
