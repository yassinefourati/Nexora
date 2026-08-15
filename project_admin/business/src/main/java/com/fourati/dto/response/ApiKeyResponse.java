package com.fourati.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String name,
        List<String> scopes,
        UUID userId,
        Instant createdAt,
        Instant expiresAt,
        Instant lastUsedAt,
        Instant revokedAt
) {
}
