package com.fourati.audit;

import com.fourati.domain.AuditLog;
import com.fourati.domain.User;
import com.fourati.platform.audit.AuditEvent;
import com.fourati.repository.AuditLogRepository;
import com.fourati.repository.UserRepository;
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
 * Verifies AuditLogEventListener resolves the actor by username against the real
 * users table, not by parsing the actor string as a UUID -- the Keycloak JWT
 * subject and this backend's users.id are independently-provisioned UUIDs that
 * never match for the same account (see AuditLogEventListener's own comment),
 * so blindly UUID-parsing the actor string previously caused a foreign-key
 * violation on every single audited write.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogEventListenerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogEventListener listener;

    @Test
    void onAuditEvent_resolvesActorByUsername_notByParsingAsUuid() {
        UUID entityId = UUID.randomUUID();
        User backendUser = new User();
        AuditEvent event = new AuditEvent("superadmin", "CREATE", "user", entityId, Instant.now());

        when(userRepository.findByUsername("superadmin")).thenReturn(Optional.of(backendUser));

        listener.onAuditEvent(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(backendUser);
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getEntityType()).isEqualTo("user");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
    }

    @Test
    void onAuditEvent_systemActor_persistsAuditLogWithNoUserFk_noCrash() {
        UUID entityId = UUID.randomUUID();
        AuditEvent event = new AuditEvent("system", "DELETE", "role", entityId, Instant.now());

        when(userRepository.findByUsername("system")).thenReturn(Optional.empty());

        listener.onAuditEvent(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();

        assertThat(saved.getUser()).isNull();
        assertThat(saved.getAction()).isEqualTo("DELETE");
        assertThat(saved.getEntityType()).isEqualTo("role");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
    }

    @Test
    void onAuditEvent_unrecognizedActor_persistsAuditLogWithNoUserFk_noCrash() {
        UUID entityId = UUID.randomUUID();
        AuditEvent event = new AuditEvent("cid:abc-123", "UPDATE", "setting", entityId, Instant.now());

        when(userRepository.findByUsername("cid:abc-123")).thenReturn(Optional.empty());

        listener.onAuditEvent(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isNull();
    }
}
