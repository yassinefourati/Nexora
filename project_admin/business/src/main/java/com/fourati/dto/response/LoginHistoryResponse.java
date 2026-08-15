package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoginHistoryResponse(
        UUID id,
        UUID userId,
        String ipAddress,
        String userAgent,
        Instant loginAt,
        Instant logoutAt,
        boolean success,
        Instant createdAt
) {
}
