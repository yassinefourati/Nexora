package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(
        @SafeInput(allowHtml = true) @NotBlank String body
) {
}
