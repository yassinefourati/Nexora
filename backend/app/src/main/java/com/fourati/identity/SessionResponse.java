package com.fourati.identity;

import java.time.Instant;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant createdAt,
        Instant expiresAt,
        Instant revokedAt,
        String ipAddress,
        String userAgent,
        Long version
) {
}
