package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CollectionCaseResponse(
        UUID id,
        UUID loanAccountId,
        UUID loanInstallmentId,
        String status,
        String stage,
        String assignedTo,
        BigDecimal overdueAmount,
        String resolutionNotes,
        Instant openedAt,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
