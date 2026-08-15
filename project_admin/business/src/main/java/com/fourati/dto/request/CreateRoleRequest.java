package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String name,

        @Size(max = 255)
        @SafeInput
        String description,

        Boolean system
) {
}
