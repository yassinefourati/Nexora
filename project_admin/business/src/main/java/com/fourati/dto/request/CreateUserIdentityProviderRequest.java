package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserIdentityProviderRequest(

        @NotNull
        UUID userId,

        @NotBlank
        @Size(max = 50)
        @SafeInput
        String provider,

        @NotBlank
        @Size(max = 255)
        @SafeInput
        String providerUserId,

        @SafeInput(allowHtml = true)
        String rawProfile
) {
}
