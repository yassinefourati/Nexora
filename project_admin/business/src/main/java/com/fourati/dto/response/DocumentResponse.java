package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String documentType,
        String category,
        String fileName,
        String storageKey,
        String contentType,
        Long sizeBytes,
        String status,
        Instant uploadedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
