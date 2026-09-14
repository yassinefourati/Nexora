package com.fourati.access;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(

        @Size(max = 255)
        @SafeInput
        String description,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
