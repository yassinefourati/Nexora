package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID menuId,
        UUID parentMenuItemId,
        String label,
        String routePath,
        String moduleKey,
        String icon,
        int sortOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
