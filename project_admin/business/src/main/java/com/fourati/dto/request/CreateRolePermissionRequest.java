package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRolePermissionRequest(

        @NotNull
        UUID roleId,

        @NotNull
        UUID permissionId,

        String conditions
) {
}
