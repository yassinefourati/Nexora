package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanApprovalResponse(
        UUID id,
        UUID loanApplicationId,
        UUID underwritingCaseId,
        String status,
        BigDecimal approvedAmount,
        Integer approvedTermMonths,
        BigDecimal interestRate,
        String approvedBy,
        String rejectionReason,
        Instant expiresAt,
        Instant approvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
