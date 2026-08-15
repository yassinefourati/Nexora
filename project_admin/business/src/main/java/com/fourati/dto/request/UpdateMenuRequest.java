package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMenuRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @SafeInput
        String description,

        Boolean active
) {
}
