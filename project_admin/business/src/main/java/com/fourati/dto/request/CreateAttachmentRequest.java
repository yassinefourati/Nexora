package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAttachmentRequest(
        @SafeInput @NotBlank @Size(max = 100) String entityType,
        @NotNull UUID entityId,
        UUID uploadedBy,
        @SafeInput @NotBlank @Size(max = 255) String fileName,
        @SafeInput @NotBlank String fileUrl,
        @SafeInput @Size(max = 150) String mimeType,
        Long sizeBytes
) {
}
