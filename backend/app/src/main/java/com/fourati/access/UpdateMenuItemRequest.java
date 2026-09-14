package com.fourati.access;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateMenuItemRequest(

        UUID parentMenuItemId,

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String label,

        @Size(max = 255)
        @SafeInput
        String routePath,

        @Size(max = 100)
        @SafeInput
        String moduleKey,

        @Size(max = 100)
        @SafeInput
        String icon,

        Integer sortOrder,

        Boolean active,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
