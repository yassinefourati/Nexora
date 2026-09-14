package com.fourati.access;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMenuRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @SafeInput
        String description,

        Boolean active,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
