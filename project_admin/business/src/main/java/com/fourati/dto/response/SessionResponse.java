package com.fourati.dto.response;

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
        String userAgent
) {
}
