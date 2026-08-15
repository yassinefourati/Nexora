package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RolePermissionResponse(
        UUID id,
        UUID roleId,
        UUID permissionId,
        String conditions,
        Instant createdAt,
        Instant updatedAt
) {
}
