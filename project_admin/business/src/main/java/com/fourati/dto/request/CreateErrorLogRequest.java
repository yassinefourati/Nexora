package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload used internally by the application to record a new error log entry.
 * There is no public write endpoint for this resource.
 */
public record CreateErrorLogRequest(
        @Size(max = 150)
        @SafeInput
        String source,

        @NotBlank
        @SafeInput
        String errorMessage,

        @SafeInput
        String stackTrace,

        @SafeInput
        String context,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String severity
) {
}
