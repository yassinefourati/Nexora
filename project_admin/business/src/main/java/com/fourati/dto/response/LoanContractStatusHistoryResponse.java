package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanContractStatusHistoryResponse(
        UUID id,
        UUID loanContractId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
