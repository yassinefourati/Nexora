package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID documentId,
        int versionNumber,
        String storageKey,
        Instant uploadedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
