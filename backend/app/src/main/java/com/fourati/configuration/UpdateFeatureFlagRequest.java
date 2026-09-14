package com.fourati.configuration;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateFeatureFlagRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @Size(max = 500)
        @SafeInput
        String description,

        @NotNull
        Boolean enabled,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
