package com.fourati.access;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/** Body of {@code POST /users/{id}/roles}. */
public record AssignRoleRequest(
        @NotNull UUID roleId,
        /** Optional organisation scope; null means a global grant. */
        UUID organizationId,
        /** Optional expiry; must be in the future when present. */
        Instant expiresAt
) {
}
