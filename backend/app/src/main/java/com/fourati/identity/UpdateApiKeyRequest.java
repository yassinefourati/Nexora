package com.fourati.identity;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record UpdateApiKeyRequest(

        @Size(max = 150)
        @SafeInput
        String name,

        @Size(max = 50)
        List<String> scopes,

        Instant expiresAt,

        Instant revokedAt,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
