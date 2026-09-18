package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AmlScreeningResponse(
        UUID id,
        UUID kycCaseId,
        String screeningType,
        String result,
        Instant screenedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
