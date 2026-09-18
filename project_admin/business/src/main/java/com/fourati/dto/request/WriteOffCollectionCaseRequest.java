package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WriteOffCollectionCaseRequest(
        @NotBlank
        @Size(max = 1000)
        @SafeInput
        String resolutionNotes
) {
}
