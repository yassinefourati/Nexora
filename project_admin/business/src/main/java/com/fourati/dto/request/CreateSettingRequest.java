package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSettingRequest(

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String scope,

        UUID organizationId,

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String key,

        String value,

        @Size(max = 500)
        @SafeInput
        String description,

        @NotNull
        Boolean editable
) {
}
