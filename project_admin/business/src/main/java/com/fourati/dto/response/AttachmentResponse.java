package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        String entityType,
        UUID entityId,
        UUID uploadedBy,
        String fileName,
        String fileUrl,
        String mimeType,
        Long sizeBytes,
        Instant createdAt,
        Instant updatedAt
) {
}
