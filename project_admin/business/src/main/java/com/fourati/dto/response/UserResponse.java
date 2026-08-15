package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String status,
        boolean superuser,
        int failedLoginAttempts,
        Instant lockedUntil,
        Instant lastLoginAt,
        Instant passwordChangedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
