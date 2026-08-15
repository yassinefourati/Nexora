package com.fourati.integration;

import com.fourati.domain.AuditLog;
import com.fourati.domain.User;
import com.fourati.dto.request.CreateApiKeyRequest;
import com.fourati.dto.response.ApiKeyCreatedResponse;
import com.fourati.platform.util.HashUtils;
import com.fourati.repository.ApiKeyRepository;
import com.fourati.repository.AuditLogRepository;
import com.fourati.repository.UserRepository;
import com.fourati.service.ApiKeyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
// MOCK (not NONE): AdminBackendPlatformAutoConfiguration (which registers AuditAspect,
// CommonProperties, GlobalExceptionHandler, etc.) is @ConditionalOnWebApplication --
// WebEnvironment.NONE would silently skip it and the audit pipeline under test.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class ApiKeyAuditIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

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
        user.setPasswordHash("irrelevant-for-this-test");
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
        // AuditLogEventListener runs synchronously (@EventListener, REQUIRES_NEW) but in a
        // separate transaction from the one that just committed the ApiKey insert; poll
        // briefly rather than asserting immediately, to avoid a flaky race on slower CI hosts.
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("apiKey", response.id());
            assertThat(logs).isNotEmpty();
            assertThat(logs.get(0).getAction()).isEqualTo("CREATE");
        });
    }
}
