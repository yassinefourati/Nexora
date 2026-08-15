package com.fourati.audit;

import com.fourati.domain.AuditLog;
import com.fourati.platform.audit.AuditEvent;
import com.fourati.repository.AuditLogRepository;
import com.fourati.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists {@link AuditEvent}s (published by the domain-agnostic
 * {@code platform.audit.AuditAspect}) into the audit_logs table. This is the
 * domain-aware half of the audit trail — it's what actually populates
 * AuditLogRepository, keeping the platform.. aspect itself free of any
 * dependency on this app's own entities/repositories.
 */
@Component
@RequiredArgsConstructor
public class AuditLogEventListener {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        AuditLog entry = new AuditLog();
        entry.setAction(event.action());
        entry.setEntityType(event.entityType());
        entry.setEntityId(event.entityId());

        // event.actor() is now the Keycloak preferred_username (see AuditAspect.resolveActor),
        // resolved to a real backend User row the same way the frontend's
        // useCurrentBackendUser does -- Keycloak's SSO subject and this backend's users.id
        // are independently-provisioned UUIDs that never match for the same account, so
        // blindly UUID-parsing the actor string and using it as a User FK (the previous
        // approach) silently produced a foreign-key violation on every single audited write.
        userRepository.findByUsername(event.actor()).ifPresent(entry::setUser);
        // "system", "cid:...", or an unrecognized username are not linked to a User FK --
        // the human-readable value is already captured in the [AUDIT] log line AuditAspect emits.

        auditLogRepository.save(entry);
    }
}
