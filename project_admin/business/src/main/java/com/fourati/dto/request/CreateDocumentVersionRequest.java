package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateDocumentVersionRequest(
        @NotNull
        UUID documentId,

        @NotNull
        @Min(1)
        Integer versionNumber,

        @NotBlank
        @Size(max = 500)
        @SafeInput
        String storageKey
) {
}
