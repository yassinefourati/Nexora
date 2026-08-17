package com.fourati.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record RiskAssessmentFactorResponse(
        UUID id,
        UUID riskAssessmentId,
        String factorType,
        BigDecimal factorValue,
        BigDecimal weight
) {
}
