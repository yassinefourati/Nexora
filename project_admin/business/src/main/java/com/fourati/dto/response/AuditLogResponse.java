package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        String beforeData,
        String afterData,
        String ipAddress,
        Instant createdAt
) {
}
