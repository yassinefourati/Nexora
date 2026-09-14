package com.fourati.configuration;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagResponse(
        UUID id,
        String key,
        String name,
        String description,
        UUID organizationId,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
