package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CustomerAddressResponse(
        UUID id,
        UUID customerId,
        String addressType,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        boolean primary,
        Instant createdAt,
        Instant updatedAt
) {
}
