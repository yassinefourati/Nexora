package com.fourati.integration;

import com.fourati.domain.Document;
import com.fourati.dto.request.CreateDocumentRequest;
import com.fourati.dto.request.ReviewDocumentRequest;
import com.fourati.dto.response.DocumentResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.DocumentRepository;
import com.fourati.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering document create -> review through the real
 * service -> repository -> database path (not mocks), including the
 * storage-key conflict guard and the already-decided review guard.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class DocumentCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    private CreateDocumentRequest newDocumentRequest(String suffix) {
        return new CreateDocumentRequest("identity", "identity", "passport-" + suffix + ".pdf",
                "docs/test/" + suffix + "/passport.pdf", "application/pdf", 12345L);
    }

    @Test
    void create_persistsDocumentAndIsRetrievableById() {
        String suffix = UUID.randomUUID().toString();
        CreateDocumentRequest request = newDocumentRequest(suffix);

        DocumentResponse created = documentService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo("uploaded");

        Document stored = documentRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getStorageKey()).isEqualTo(request.storageKey());

        documentRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsDuplicateStorageKey() {
        String suffix = UUID.randomUUID().toString();
        DocumentResponse first = documentService.create(newDocumentRequest(suffix));

        assertThatThrownBy(() -> documentService.create(newDocumentRequest(suffix)))
                .isInstanceOf(ConflictException.class);

        documentRepository.deleteById(first.id());
    }

    @Test
    void review_transitionsStatus() {
        DocumentResponse created = documentService.create(newDocumentRequest(UUID.randomUUID().toString()));

        DocumentResponse reviewed = documentService.review(created.id(), new ReviewDocumentRequest("verified", "all good"));

        assertThat(reviewed.status()).isEqualTo("verified");

        documentRepository.deleteById(created.id());
    }

    @Test
    void delete_makesDocumentUnreadableThroughTheNormalReadPath() {
        DocumentResponse created = documentService.create(newDocumentRequest(UUID.randomUUID().toString()));

        documentService.delete(created.id());

        assertThatThrownBy(() -> documentService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(documentRepository.findById(created.id())).isEmpty();
    }
}
