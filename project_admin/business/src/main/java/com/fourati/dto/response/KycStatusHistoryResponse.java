package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KycStatusHistoryResponse(
        UUID id,
        UUID kycCaseId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
