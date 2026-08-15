package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RoleMenuResponse(
        UUID id,
        UUID roleId,
        UUID menuItemId,
        boolean canView,
        Instant createdAt,
        Instant updatedAt
) {
}
