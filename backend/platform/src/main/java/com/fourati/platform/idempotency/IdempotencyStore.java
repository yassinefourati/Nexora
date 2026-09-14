package com.fourati.platform.idempotency;

import java.time.Duration;
import java.util.Optional;

/**
 * Where idempotency keys and their captured responses live. In-memory for a
 * single replica, Redis when {@code fourati.common.store=redis} so a retry
 * that lands on a different replica still replays the original response.
 */
public interface IdempotencyStore {

    /** A completed response captured for replay. */
    record CachedResponse(int status, String contentType, String body) {
    }

    /** Result of a lookup: either nothing, an in-flight marker, or a completed response. */
    sealed interface Entry permits InFlight, Completed {
    }

    record InFlight() implements Entry {
    }

    record Completed(CachedResponse response) implements Entry {
    }

    /**
     * Atomically claims {@code key} for the caller. Returns empty when the claim
     * succeeded (the caller must process and then {@link #complete} or
     * {@link #release}); otherwise returns what is already stored.
     */
    Optional<Entry> claim(String key, Duration inFlightTtl);

    void complete(String key, CachedResponse response, Duration ttl);

    /** Drops an in-flight claim after a failure so a retry can proceed. */
    void release(String key);
}
