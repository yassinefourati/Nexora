package com.fourati.integration;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.junit.jupiter.api.BeforeEach;

import com.fourati.identity.UserStatus;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.integration.support.TransactionalScenario;
import com.fourati.integration.support.TransactionalScenario.SimulatedFailure;
import com.fourati.platform.security.CurrentUser;
import com.fourati.audit.internal.persistence.AuditLogRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.identity.internal.application.CreateUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ADR 0007: the audit row is written inside the business transaction.
 *
 * <ul>
 *   <li>a committed audited write has its audit row the moment the call returns,
 *       with no polling and no second transaction;</li>
 *   <li>a rolled-back audited write leaves no audit row, because the row was part
 *       of the same transaction;</li>
 *   <li>the row's actor is the exact user carried by the {@code CurrentUser}
 *       principal (ADR 0006), not a name lookup.</li>
 * </ul>
 */
class AuditTransactionIntegrationTest extends AbstractIntegrationTest {

    @Autowired private CreateUser createUser;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private TransactionalScenario scenario;

    private final List<UUID> createdUsers = new ArrayList<>();

    private static CreateUserRequest newUser() {
        String suffix = UUID.randomUUID().toString();
        return new CreateUserRequest("audit-tx-" + suffix, "audit-tx-" + suffix + "@example.com",
                "Audit", "Tx", UserStatus.ACTIVE, false);
    }

    @BeforeEach
    void asAdmin() {
        SecurityContextHolder.getContext().setAuthentication(TestAuthentications.token(null, "root", "admin"));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        for (UUID id : createdUsers) {
            auditLogRepository.deleteAll(auditLogRepository.findByEntityTypeAndEntityId("user", id));
            userRepository.deleteById(id);
        }
        createdUsers.clear();
    }

    @Test
    void committedAuditedWrite_hasItsAuditRow_immediately() {
        UserResponse created = createUser.execute(newUser());
        createdUsers.add(created.id());

        List<AuditLog> rows = auditLogRepository.findByEntityTypeAndEntityId("user", created.id());

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getAction()).isEqualTo("CREATE");
    }

    @Test
    void rolledBackAuditedWrite_leavesNoAuditRow_andNoUser() {
        long auditRowsBefore = auditLogRepository.count();
        CreateUserRequest request = newUser();

        assertThatThrownBy(() -> scenario.createUserThenFail(request))
                .isInstanceOf(SimulatedFailure.class);

        assertThat(userRepository.findByUsername(request.username())).isEmpty();
        assertThat(auditLogRepository.count()).isEqualTo(auditRowsBefore);
    }

    @Test
    void auditRow_isAttributedToTheCurrentUserPrincipal() {
        User actor = new User();
        actor.setUsername("audit-actor-" + UUID.randomUUID());
        actor.setEmail(actor.getUsername() + "@example.com");
        actor.setStatus(UserStatus.ACTIVE);
        actor = userRepository.save(actor);
        createdUsers.add(actor.getId());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new CurrentUser(actor.getId(), "sub-" + actor.getUsername(), actor.getUsername(), false), null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        UserResponse created = createUser.execute(newUser());
        createdUsers.add(created.id());

        List<AuditLog> rows = auditLogRepository.findByEntityTypeAndEntityId("user", created.id());
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getUserId()).isEqualTo(actor.getId());
    }
}
