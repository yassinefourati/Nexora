package com.fourati.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Returned exactly once, from the create-API-key endpoint. {@code secret} is the raw,
 * unhashed key material — the server never persists it and cannot return it again.
 * The caller must store it securely; only {@code keyHash} lives in the DB.
 */
public record ApiKeyCreatedResponse(
        UUID id,
        String name,
        String secret,
        List<String> scopes,
        UUID userId,
        Instant createdAt,
        Instant expiresAt
) {
}
