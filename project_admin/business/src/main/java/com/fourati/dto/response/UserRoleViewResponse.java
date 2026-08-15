package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

/** Read-only projection of {@code user_roles_view}. */
public record UserRoleViewResponse(
        UUID userId,
        String username,
        UUID roleId,
        String roleName,
        UUID organizationId,
        String organizationName,
        Instant expiresAt
) {
}
