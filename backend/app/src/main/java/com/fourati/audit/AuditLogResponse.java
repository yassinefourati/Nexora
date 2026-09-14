package com.fourati.audit;

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
        Instant createdAt,
        Long version
) {
}
