package com.fourati.integration;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.CreateApiKeyRequest;
import com.fourati.identity.ApiKeyCreatedResponse;
import com.fourati.platform.util.HashUtils;
import com.fourati.identity.internal.persistence.ApiKeyRepository;
import com.fourati.audit.internal.persistence.AuditLogRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.identity.internal.application.ApiKeyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers), proving two
 * previously-broken flows now work at runtime (not just at compile time):
 *
 * 1. API key creation: the server generates the secret + hash itself, persists only
 *    prefix/keyHash, and the raw secret is retrievable only from the immediate
 *    create() response — never by re-reading the stored row.
 *
 * 2. The audit trail pipeline: AuditAspect (on the @Audited ApiKeyService.create method)
 *    publishes an AuditEvent, which AuditLogEventListener persists into audit_logs. This
 *    is exactly the kind of bug the original review found — it compiled fine but had zero
 *    callers wiring events to persistence, so the table was always empty at runtime.
 */
// The base class boots a MOCK web environment (not NONE): the platform auto-configuration
// that registers AuditAspect, CommonProperties and GlobalExceptionHandler is
// @ConditionalOnWebApplication, and NONE would silently skip the audit pipeline under test.
class ApiKeyAuditIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void seedUser() {
        User user = new User();
        user.setUsername("apikey-audit-test-" + UUID.randomUUID());
        user.setEmail(user.getUsername() + "@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        testUser = userRepository.save(user);
    }

    @AfterEach
    void cleanup() {
        apiKeyRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.delete(testUser);
    }

    @Test
    void createApiKey_persistsHashedSecretAndAuditLogRow() {
        CreateApiKeyRequest request = new CreateApiKeyRequest(
                "Integration Test Key",
                List.of("read", "write"),
                testUser.getId(),
                Instant.now().plusSeconds(3600));

        ApiKeyCreatedResponse response = apiKeyService.create(request);

        // --- Bug #1: server-side key material ---
        assertThat(response.secret()).isNotBlank();
        assertThat(response.secret()).startsWith("fourati_");

        String expectedHash = HashUtils.sha256(response.secret());

        var stored = apiKeyRepository.findById(response.id()).orElseThrow();
        assertThat(stored.getKeyHash()).isEqualTo(expectedHash);
        assertThat(stored.getKeyHash()).isNotEqualTo(response.secret());

        assertThat(apiKeyRepository.existsByKeyHash(expectedHash)).isTrue();

        // --- Bug #2: audit trail actually persists a row ---
        // The audit row is written inside the same transaction as the ApiKey insert
        // (ADR 0007), so it is visible the moment create() returns: no polling.
        List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("apiKey", response.id());
        assertThat(logs).isNotEmpty();
        assertThat(logs.get(0).getAction()).isEqualTo("CREATE");
    }
}
