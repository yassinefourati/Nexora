package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAuthLogRequest(

        UUID userId,

        @NotBlank
        @Size(max = 30)
        @SafeInput
        String eventType,

        @Size(max = 45)
        @SafeInput
        String ipAddress,

        @Size(max = 500)
        @SafeInput
        String userAgent,

        @SafeInput(allowHtml = true)
        String metadata
) {
}
