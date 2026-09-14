package com.fourati.identity;

import java.time.Instant;

/**
 * Used to revoke an active session ahead of its natural expiry.
 */
public record UpdateSessionRequest(
        Instant revokedAt,
        /** Version the client last read; when present the update is refused with 409 if the row changed since. */
        Long version
) {
}
