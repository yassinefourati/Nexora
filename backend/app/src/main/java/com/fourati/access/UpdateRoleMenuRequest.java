package com.fourati.access;

import jakarta.validation.constraints.NotNull;

public record UpdateRoleMenuRequest(

        @NotNull
        Boolean canView,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
