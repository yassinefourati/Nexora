package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ErrorLogResponse(
        UUID id,
        String source,
        String errorMessage,
        String stackTrace,
        String context,
        String severity,
        Instant createdAt,
        Instant updatedAt
) {
}
