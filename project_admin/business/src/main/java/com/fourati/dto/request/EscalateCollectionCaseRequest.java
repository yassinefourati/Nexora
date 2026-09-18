package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscalateCollectionCaseRequest(
        @NotBlank
        @Size(max = 20)
        @SafeInput
        String stage
) {
}
