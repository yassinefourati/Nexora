package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanNotificationResponse(
        UUID id,
        UUID loanApplicationId,
        UUID notificationId,
        String eventType,
        String title,
        String body,
        String channel,
        Instant createdAt
) {
}
