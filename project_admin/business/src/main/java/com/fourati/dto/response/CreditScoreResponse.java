package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreditScoreResponse(
        UUID id,
        UUID creditCheckId,
        int score,
        String scoreModel,
        Instant scoredAt
) {
}
