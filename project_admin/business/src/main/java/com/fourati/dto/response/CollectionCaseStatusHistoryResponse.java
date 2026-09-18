package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CollectionCaseStatusHistoryResponse(
        UUID id,
        UUID collectionCaseId,
        String fromStatus,
        String toStatus,
        String reason,
        Instant changedAt
) {
}
