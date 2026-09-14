package com.fourati.identity;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Pre-provision a user before their first Keycloak sign-in (ADR 0001). There
 * is no password: authentication is the identity provider's job.
 */
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
        @Size(max = 100)
        @SafeInput
        String firstName,

        @NotBlank
        @Size(max = 100)
        @SafeInput
        String lastName,

        /** Defaults to {@code active} when omitted. */
        UserStatus status,

        Boolean superuser
) {
}
