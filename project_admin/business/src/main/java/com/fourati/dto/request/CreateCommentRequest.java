package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentRequest(
        @SafeInput @NotBlank @Size(max = 100) String entityType,
        @NotNull UUID entityId,
        @SafeInput(allowHtml = true) @NotBlank String body,
        UUID parentCommentId
) {
}
