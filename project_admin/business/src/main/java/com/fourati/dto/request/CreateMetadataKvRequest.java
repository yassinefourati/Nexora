package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateMetadataKvRequest(
        @SafeInput @NotBlank @Size(max = 100) String entityType,
        @NotNull UUID entityId,
        @SafeInput @NotBlank @Size(max = 150) String key,
        @SafeInput(allowHtml = true) @NotBlank String value
) {
}
