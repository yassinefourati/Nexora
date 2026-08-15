package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequest(

        UUID templateId,

        @NotBlank
        @Size(max = 255)
        @SafeInput
        String title,

        @NotBlank
        @SafeInput
        String body,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String channel,

        @SafeInput
        String data
) {
}
