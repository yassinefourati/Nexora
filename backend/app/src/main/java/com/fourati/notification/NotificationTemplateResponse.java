package com.fourati.notification;

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
        Instant updatedAt,
        Long version
) {
}
