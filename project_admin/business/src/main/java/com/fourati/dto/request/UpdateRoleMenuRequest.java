package com.fourati.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleMenuRequest(

        @NotNull
        Boolean canView
) {
}
