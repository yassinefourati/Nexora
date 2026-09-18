package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LoanApplicationCoApplicantResponse(
        UUID id,
        UUID loanApplicationId,
        UUID customerId,
        String relationshipType,
        Instant createdAt,
        Instant updatedAt
) {
}
