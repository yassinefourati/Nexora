package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FraudCheckResponse(
        UUID id,
        UUID loanApplicationId,
        String status,
        Integer fraudScore,
        Instant checkedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
