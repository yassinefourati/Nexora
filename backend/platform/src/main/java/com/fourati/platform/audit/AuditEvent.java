package com.fourati.platform.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic, domain-agnostic record of an @Audited method invocation. AuditAspect
 * publishes this via ApplicationEventPublisher; a domain-side listener is
 * responsible for turning it into a persisted audit_logs row, if desired. This
 * keeps platform.. free of any dependency on this app's own domain/repository code.
 *
 * The actor is described three ways so the listener never has to guess:
 * {@code actorUserId} is exact when the request carried a resolved
 * {@link com.fourati.platform.security.CurrentUser}; {@code actorSubject} and
 * {@code actor} are the fallbacks for events raised without one.
 */
public record AuditEvent(
        /** Human-readable actor: backend username, "system", or "cid:<correlation id>". */
        String actor,
        /** Identity-provider subject of the actor when authenticated by JWT; null otherwise. */
        String actorSubject,
        /** Backend users.id of the actor when the principal carried one; null otherwise. */
        UUID actorUserId,
        String action,
        String entityType,
        UUID entityId,
        Instant occurredAt
) {
}
