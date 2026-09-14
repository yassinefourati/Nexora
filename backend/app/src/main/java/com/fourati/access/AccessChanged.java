package com.fourati.access;

import java.util.UUID;

/**
 * Published inside the transaction that changes who may do what: a role
 * grant or revocation, a permission granted to or removed from a role, or a
 * full access revocation. Listeners run after commit on every replica that
 * shares the event's effects (ADR 0008): today that is cache invalidation of
 * the effective-permission set; in Phase 5 it also refreshes the principal's
 * authorities.
 *
 * @param userId the user whose access changed, when the change is user-scoped; null for role-wide changes
 * @param roleId the role whose grants changed, when known; null otherwise
 * @param reason a short machine-readable tag such as {@code ROLE_ASSIGNED} or {@code PERMISSIONS_GRANTED}
 */
public record AccessChanged(UUID userId, UUID roleId, String reason) {
}
