package com.fourati.access;

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
