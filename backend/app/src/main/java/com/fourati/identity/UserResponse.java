package com.fourati.identity;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        boolean superuser,
        Instant lastLoginAt,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
