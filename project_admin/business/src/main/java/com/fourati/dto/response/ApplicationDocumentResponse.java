package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ApplicationDocumentResponse(
        UUID id,
        UUID loanApplicationId,
        UUID documentId,
        String requirementType,
        Instant createdAt,
        Instant updatedAt
) {
}
