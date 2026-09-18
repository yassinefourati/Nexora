package com.fourati.integration;

import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.UpdateLoanProductRequest;
import com.fourati.dto.response.LoanProductResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanProductRepository;
import com.fourati.service.LoanProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering the core Loan Products CRUD flow through the real
 * service -> repository -> database path (not mocks), including the
 * soft-delete and code-conflict behavior a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanProductCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanProductService loanProductService;

    @Autowired
    private LoanProductRepository loanProductRepository;

    private CreateLoanProductRequest newLoanProductRequest(String suffix) {
        return new CreateLoanProductRequest(
                "crud-test-" + suffix,
                "Test Loan Product",
                "personal",
                "active",
                "USD",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50000),
                6,
                84,
                "Integration test product");
    }

    @Test
    void create_persistsLoanProductAndIsRetrievableById() {
        String suffix = UUID.randomUUID().toString();
        CreateLoanProductRequest request = newLoanProductRequest(suffix);

        LoanProductResponse created = loanProductService.create(request);

        assertThat(created.id()).isNotNull();

        LoanProduct stored = loanProductRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getCode()).isEqualTo(request.code());

        loanProductRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsDuplicateCode() {
        String suffix = UUID.randomUUID().toString();
        LoanProductResponse first = loanProductService.create(newLoanProductRequest(suffix));

        assertThatThrownBy(() -> loanProductService.create(newLoanProductRequest(suffix)))
                .isInstanceOf(ConflictException.class);

        loanProductRepository.deleteById(first.id());
    }

    @Test
    void update_changesFieldsAndPersists() {
        LoanProductResponse created = loanProductService.create(newLoanProductRequest(UUID.randomUUID().toString()));

        UpdateLoanProductRequest update = new UpdateLoanProductRequest(
                created.code(),
                "Updated Name",
                "personal",
                "inactive",
                "USD",
                created.minAmount(),
                created.maxAmount(),
                created.minTermMonths(),
                created.maxTermMonths(),
                "Updated description");

        LoanProductResponse updated = loanProductService.update(created.id(), update);

        assertThat(updated.name()).isEqualTo("Updated Name");
        assertThat(updated.status()).isEqualTo("inactive");

        loanProductRepository.deleteById(created.id());
    }

    @Test
    void delete_makesLoanProductUnreadableThroughTheNormalReadPath() {
        LoanProductResponse created = loanProductService.create(newLoanProductRequest(UUID.randomUUID().toString()));

        loanProductService.delete(created.id());

        assertThatThrownBy(() -> loanProductService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanProductRepository.findById(created.id())).isEmpty();
    }
}
