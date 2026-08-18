package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CustomerConsentResponse(
        UUID id,
        UUID customerId,
        String consentType,
        boolean granted,
        Instant grantedAt,
        Instant revokedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
