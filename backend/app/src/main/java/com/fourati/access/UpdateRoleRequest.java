package com.fourati.access;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String name,

        @Size(max = 255)
        @SafeInput
        String description,

        Boolean system,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
