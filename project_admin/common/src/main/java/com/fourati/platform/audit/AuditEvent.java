package com.fourati.platform.audit;

import java.time.Instant;
import java.util.UUID;

/**
 * Generic, domain-agnostic record of an @Audited method invocation. AuditAspect
 * publishes this via ApplicationEventPublisher; a domain-side listener is
 * responsible for turning it into a persisted audit_logs row, if desired. This
 * keeps platform.. free of any dependency on this app's own domain/repository code.
 */
public record AuditEvent(
        String actor,
        String action,
        String entityType,
        UUID entityId,
        Instant occurredAt
) {
}
