package com.fourati.identity;

import java.time.Instant;
import java.util.UUID;

public record UserIdentityProviderResponse(
        UUID id,
        UUID userId,
        String provider,
        String providerUserId,
        String rawProfile,
        Instant linkedAt,
        Long version
) {
}
