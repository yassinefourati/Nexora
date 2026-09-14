package com.fourati.access;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMenuPermissionRequest(

        @NotNull
        UUID menuItemId,

        @NotNull
        UUID permissionId
) {
}
