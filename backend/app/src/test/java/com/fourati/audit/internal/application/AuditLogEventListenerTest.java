package com.fourati.audit.internal.application;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.audit.internal.persistence.AuditLogRepository;
import com.fourati.identity.IdentityFacade;
import com.fourati.platform.audit.AuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The listener is the only thing that writes audit_logs. It hands the actor
 * triple to the identity module, stores whatever id comes back, and never
 * fails the audit write because an actor is unknown.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogEventListenerTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private IdentityFacade identity;

    @InjectMocks
    private AuditLogEventListener listener;

    private static AuditEvent event(String actor, String subject, UUID userId) {
        return new AuditEvent(actor, subject, userId, "CREATE", "user", UUID.randomUUID(), Instant.now());
    }

    private AuditLog savedRow() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void resolvedActor_isStoredOnTheRow() {
        UUID id = UUID.randomUUID();
        when(identity.resolveActor(id, "sub-1", "alice")).thenReturn(Optional.of(id));

        listener.onAuditEvent(event("alice", "sub-1", id));

        AuditLog row = savedRow();
        assertThat(row.getUserId()).isEqualTo(id);
        assertThat(row.getAction()).isEqualTo("CREATE");
        assertThat(row.getEntityType()).isEqualTo("user");
    }

    @Test
    void unknownActor_persistsRowWithNoUser_noCrash() {
        when(identity.resolveActor(null, null, "system")).thenReturn(Optional.empty());

        listener.onAuditEvent(event("system", null, null));

        assertThat(savedRow().getUserId()).isNull();
    }
}
