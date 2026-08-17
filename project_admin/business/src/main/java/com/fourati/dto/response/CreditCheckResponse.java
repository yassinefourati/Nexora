package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CreditCheckResponse(
        UUID id,
        UUID loanApplicationId,
        UUID customerId,
        String status,
        Instant requestedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
