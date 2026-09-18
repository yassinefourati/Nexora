package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanDisbursementStatusHistoryResponse(
        UUID id,
        UUID loanDisbursementId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
