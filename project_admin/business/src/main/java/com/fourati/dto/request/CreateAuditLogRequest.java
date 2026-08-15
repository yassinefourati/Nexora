package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAuditLogRequest(

        UUID userId,

        @NotBlank
        @Size(max = 50)
        @SafeInput
        String action,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String entityType,

        UUID entityId,

        @SafeInput(allowHtml = true)
        String beforeData,

        @SafeInput(allowHtml = true)
        String afterData,

        @Size(max = 45)
        @SafeInput
        String ipAddress
) {
}
