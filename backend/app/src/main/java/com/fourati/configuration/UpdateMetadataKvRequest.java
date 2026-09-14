package com.fourati.configuration;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;

public record UpdateMetadataKvRequest(
        @SafeInput(allowHtml = true) @NotBlank String value,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
