package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record NotificationTemplateResponse(
        UUID id,
        String code,
        String name,
        String subjectTemplate,
        String bodyTemplate,
        String channel,
        Instant createdAt,
        Instant updatedAt
) {
}
