package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanDisbursementResponse(
        UUID id,
        UUID loanApplicationId,
        UUID loanContractId,
        String status,
        BigDecimal amount,
        String disbursementMethod,
        String destinationAccount,
        String referenceNumber,
        String failureReason,
        Instant initiatedAt,
        Instant completedAt,
        Instant failedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
