package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;

public record UpdateMetadataKvRequest(
        @SafeInput(allowHtml = true) @NotBlank String value
) {
}
