package com.fourati.notification;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotificationRequest(

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
        String data,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
