package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record MenuPermissionResponse(
        UUID id,
        UUID menuItemId,
        UUID permissionId,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
