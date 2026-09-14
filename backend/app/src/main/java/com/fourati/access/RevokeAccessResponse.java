package com.fourati.access;

import java.util.UUID;

/** Result of {@code POST /users/{id}/access:revoke}: what was revoked, counted. */
public record RevokeAccessResponse(
        UUID userId,
        int rolesRemoved,
        int sessionsRevoked,
        int apiKeysRevoked,
        String status
) {
}
