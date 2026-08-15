package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        @SafeInput
        String email,

        @NotBlank
        @Size(min = 8, max = 255)
        String password,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String firstName,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String lastName,

        @Size(max = 20)
        @SafeInput
        String status,

        Boolean superuser
) {
}
