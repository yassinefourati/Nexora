package com.fourati.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserIdentityProviderResponse(
        UUID id,
        UUID userId,
        String provider,
        String providerUserId,
        String rawProfile,
        Instant linkedAt
) {
}
