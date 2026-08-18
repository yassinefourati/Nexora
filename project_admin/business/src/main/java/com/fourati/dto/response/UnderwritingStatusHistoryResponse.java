package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UnderwritingStatusHistoryResponse(
        UUID id,
        UUID underwritingCaseId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
