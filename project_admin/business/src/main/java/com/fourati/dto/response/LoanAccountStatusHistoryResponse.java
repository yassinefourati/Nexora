package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanAccountStatusHistoryResponse(
        UUID id,
        UUID loanAccountId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
