package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanRepaymentStatusHistoryResponse(
        UUID id,
        UUID loanRepaymentId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
