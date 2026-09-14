package com.fourati.access;

import java.time.Instant;
import java.util.UUID;

public record RoleMenuResponse(
        UUID id,
        UUID roleId,
        UUID menuItemId,
        boolean canView,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
