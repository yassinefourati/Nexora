package com.fourati.dto.request;

import java.time.Instant;

/**
 * Used to revoke an active session ahead of its natural expiry.
 */
public record UpdateSessionRequest(
        Instant revokedAt
) {
}
