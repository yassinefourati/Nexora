package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RiskAssessmentResponse(
        UUID id,
        UUID loanApplicationId,
        String status,
        Integer riskScore,
        String riskClass,
        Instant assessedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
