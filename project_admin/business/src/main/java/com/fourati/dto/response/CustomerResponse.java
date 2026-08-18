package com.fourati.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerType,
        String status,
        String firstName,
        String lastName,
        String businessName,
        LocalDate dateOfBirth,
        String nationalId,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
}
