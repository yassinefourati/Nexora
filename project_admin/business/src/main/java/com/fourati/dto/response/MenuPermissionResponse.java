package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MenuPermissionResponse(
        UUID id,
        UUID menuItemId,
        UUID permissionId,
        Instant createdAt,
        Instant updatedAt
) {
}
