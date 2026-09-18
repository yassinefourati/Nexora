package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanApprovalStatusHistoryResponse(
        UUID id,
        UUID loanApprovalId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
