package com.fourati.audit.internal.application;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.audit.internal.persistence.AuditLogRepository;
import com.fourati.identity.IdentityFacade;
import com.fourati.platform.audit.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Persists {@link AuditEvent}s (published by the domain-agnostic
 * {@code platform.audit.AuditAspect}) into the audit_logs table. This is the
 * domain-aware half of the audit trail — it's what actually populates
 * AuditLogRepository, keeping the platform aspect itself free of any
 * dependency on this app's own entities/repositories.
 *
 * The row is written <b>inside the business transaction</b> (ADR 0007): the
 * aspect publishes while the transaction is open (see {@code AuditConfig} for
 * the advisor ordering that guarantees it) and this listener runs in the
 * BEFORE_COMMIT phase, so the audited write and its audit row commit or roll
 * back together. No second connection, no async executor, no window in which
 * a committed write has no audit row. {@code fallbackExecution} covers
 * {@code @Audited} methods invoked outside any transaction, where the
 * repository save opens its own.
 *
 * The actor is resolved by the identity module ({@link IdentityFacade#resolveActor}):
 * the exact user id carried by the principal first, then the identity-provider
 * subject, then the username. "system", "cid:..." or an unknown actor leave
 * {@code user_id} null; the human-readable value is already in the [AUDIT] log line.
 */
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final AuditLogRepository auditLogRepository;
    private final IdentityFacade identity;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onAuditEvent(AuditEvent event) {
        AuditLog entry = new AuditLog();
        entry.setAction(event.action());
        entry.setEntityType(event.entityType());
        entry.setEntityId(event.entityId());
        identity.resolveActor(event.actorUserId(), event.actorSubject(), event.actor()).ifPresent(entry::setUserId);
        auditLogRepository.save(entry);
    }
}
