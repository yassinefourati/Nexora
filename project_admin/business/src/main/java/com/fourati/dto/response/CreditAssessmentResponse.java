package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditAssessmentResponse(
        UUID id,
        UUID creditCheckId,
        BigDecimal debtToIncomeRatio,
        String decision,
        Instant assessedAt
) {
}
