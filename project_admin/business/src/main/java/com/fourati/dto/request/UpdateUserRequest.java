package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        @SafeInput
        String email,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String firstName,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String lastName,

        @NotBlank
        @Size(max = 20)
        @SafeInput
        String status,

        Boolean superuser
) {
}
