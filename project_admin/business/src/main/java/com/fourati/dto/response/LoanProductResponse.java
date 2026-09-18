package com.fourati.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoanProductResponse(
        UUID id,
        String code,
        String name,
        String productType,
        String status,
        String currency,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        int minTermMonths,
        int maxTermMonths,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
