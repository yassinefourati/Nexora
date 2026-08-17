package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAmlScreeningRequest(
        @NotNull
        UUID kycCaseId,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String screeningType,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String result
) {
}
