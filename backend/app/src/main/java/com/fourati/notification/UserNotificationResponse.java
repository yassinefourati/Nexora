package com.fourati.notification;


import java.time.Instant;
import java.util.UUID;

public record UserNotificationResponse(
        UUID id,
        UUID userId,
        UUID notificationId,
        boolean read,
        Instant readAt,
        Instant deliveredAt,
        Instant createdAt,
        Long version
) {
}
