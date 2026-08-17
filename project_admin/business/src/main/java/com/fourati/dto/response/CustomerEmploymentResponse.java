package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerEmploymentResponse(
        UUID id,
        UUID customerId,
        String employerName,
        String jobTitle,
        String employmentStatus,
        BigDecimal monthlyIncome,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt
) {
}
