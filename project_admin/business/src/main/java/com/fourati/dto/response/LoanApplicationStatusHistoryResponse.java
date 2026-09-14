package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanApplicationStatusHistoryResponse(
        UUID id,
        UUID loanApplicationId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
