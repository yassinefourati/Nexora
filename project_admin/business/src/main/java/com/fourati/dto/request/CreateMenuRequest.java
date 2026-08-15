package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMenuRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String code,

        @SafeInput
        String description,

        Boolean active
) {
}
