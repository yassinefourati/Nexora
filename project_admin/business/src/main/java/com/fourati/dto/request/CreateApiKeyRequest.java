package com.fourati.dto.request;

import com.fourati.platform.security.validation.SafeInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The client never supplies the key material — the raw secret is generated
 * server-side (see ApiKeyService.create), only its SHA-256 hash is persisted
 * as {@code key_hash}, and the raw secret is returned exactly once in the
 * create response. It cannot be retrieved again afterward.
 */
public record CreateApiKeyRequest(

        @NotBlank
        @Size(max = 150)
        @SafeInput
        String name,

        @Size(max = 50)
        List<String> scopes,

        @NotNull
        UUID userId,

        Instant expiresAt
) {
}
