package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAppModuleRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @Size(max = 500)
        @SafeInput
        String description,

        @NotNull
        Boolean active
) {
}
