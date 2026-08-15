package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID templateId,
        String title,
        String body,
        String channel,
        String data,
        Instant createdAt,
        Instant updatedAt
) {
}
