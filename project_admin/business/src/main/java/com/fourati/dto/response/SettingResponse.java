package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SettingResponse(
        UUID id,
        String scope,
        UUID organizationId,
        String key,
        String value,
        String description,
        boolean editable,
        Instant createdAt,
        Instant updatedAt
) {
}
