package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record KycCheckResponse(
        UUID id,
        UUID kycCaseId,
        String checkType,
        String result,
        Instant checkedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
