package com.fourati.content;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(
        @SafeInput @NotBlank @Size(max = 100) String name,
        @SafeInput @Size(max = 20) String color,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
