package com.fourati.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerIdentificationResponse(
        UUID id,
        UUID customerId,
        String idType,
        String idNumber,
        String issuingCountry,
        LocalDate issueDate,
        LocalDate expiryDate,
        Instant createdAt,
        Instant updatedAt
) {
}
