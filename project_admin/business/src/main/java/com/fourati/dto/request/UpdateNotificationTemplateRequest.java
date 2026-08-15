package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotificationTemplateRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @Size(max = 255)
        @SafeInput
        String subjectTemplate,

        @NotBlank
        @SafeInput
        String bodyTemplate,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String channel
) {
}
