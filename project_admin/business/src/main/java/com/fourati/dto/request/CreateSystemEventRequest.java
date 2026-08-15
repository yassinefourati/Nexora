package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload used internally by the application to record a new system event.
 * There is no public write endpoint for this resource.
 */
public record CreateSystemEventRequest(
        @NotBlank
        @Size(max = 100)
        @SafeInput
        String eventType,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String severity,

        @Size(max = 100)
        @SafeInput
        String source,

        @SafeInput
        String payload
) {
}
