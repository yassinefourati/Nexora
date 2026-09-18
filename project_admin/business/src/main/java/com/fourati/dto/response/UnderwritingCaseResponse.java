package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UnderwritingCaseResponse(
        UUID id,
        UUID loanApplicationId,
        String status,
        String decision,
        String decisionReason,
        BigDecimal approvedAmount,
        Integer approvedTermMonths,
        String assignedTo,
        Instant decidedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
