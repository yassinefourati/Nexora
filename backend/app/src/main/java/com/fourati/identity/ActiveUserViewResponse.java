package com.fourati.identity;

import java.time.Instant;
import java.util.UUID;

/** Read-only projection of {@code active_users_view}. */
public record ActiveUserViewResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Instant lastLoginAt,
        Instant createdAt,
        Long version
) {
}
