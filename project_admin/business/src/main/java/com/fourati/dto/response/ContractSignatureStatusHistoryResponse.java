package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContractSignatureStatusHistoryResponse(
        UUID id,
        UUID contractSignatureId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
