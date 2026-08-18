package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanOfferStatusHistoryResponse(
        UUID id,
        UUID loanOfferId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
