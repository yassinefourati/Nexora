package com.fourati.content;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @SafeInput(allowHtml = true) @NotBlank String body,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
