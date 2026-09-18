package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DocumentRequirementResponse(
        UUID id,
        UUID loanProductId,
        String documentType,
        boolean mandatory,
        Instant createdAt,
        Instant updatedAt
) {
}
