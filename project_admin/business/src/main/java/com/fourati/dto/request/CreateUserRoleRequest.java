package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateUserRoleRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID roleId,

        UUID organizationId,

        Instant expiresAt
) {
}
