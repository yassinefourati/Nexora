package com.fourati.integration.credit;

import java.math.BigDecimal;

public record CreditBureauReportResponse(
        String bureauName,
        String reportReference,
        int rawScore,
        int normalizedScore,
        String scoreModel,
        BigDecimal debtToIncomeRatio
) {
}
