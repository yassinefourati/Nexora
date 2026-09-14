package com.fourati.identity;

import java.util.UUID;

/**
 * Published inside the transaction that changes a user's identity row in a
 * way the per-subject provisioning cache may have memorised (username, status,
 * soft deletion). Listeners run after commit and evict that cache on every
 * replica (ADR 0008).
 *
 * @param userId the backend user whose row changed
 * @param reason a short machine-readable tag such as {@code UPDATED}, {@code DELETED} or {@code SUSPENDED}
 */
public record IdentityChanged(UUID userId, String reason) {
}
