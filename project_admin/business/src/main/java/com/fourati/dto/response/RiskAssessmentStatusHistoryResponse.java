package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RiskAssessmentStatusHistoryResponse(
        UUID id,
        UUID riskAssessmentId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
