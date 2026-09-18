package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreditCheckStatusHistoryResponse(
        UUID id,
        UUID creditCheckId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
