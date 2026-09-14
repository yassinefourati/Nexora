package com.fourati.identity;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

        @NotNull
        UserStatus status,

        Boolean superuser,

        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
