package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteKycCaseRequest(
        @NotBlank
        @Size(max = 20)
        @SafeInput
        String outcome,

        @Size(max = 500)
        @SafeInput
        String reason
) {
}
