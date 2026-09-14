package com.fourati.access;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateRoleMenuRequest(

        @NotNull
        UUID roleId,

        @NotNull
        UUID menuItemId,

        Boolean canView
) {
}
