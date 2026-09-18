package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDocumentRequest(
        @NotBlank
        @Size(max = 30)
        @SafeInput
        String documentType,

        @NotBlank
        @Size(max = 30)
        @SafeInput
        String category,

        @NotBlank
        @Size(max = 255)
        @SafeInput
        String fileName,

        @NotBlank
        @Size(max = 500)
        @SafeInput
        String storageKey,

        @Size(max = 100)
        @SafeInput
        String contentType,

        @Min(0)
        Long sizeBytes
) {
}
