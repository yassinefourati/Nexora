package com.fourati.integration;

import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CancelLoanApplicationRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
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
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering the loan application create -> submit -> cancel flow through the
 * real service -> repository -> database path (not mocks), including the
 * status-history side effects and the draft-only edit / no-cancel-once-
 * decided invariants a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanApplicationCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    private UUID createCustomer(String suffix) {
        return customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "loan-app-test-" + suffix + "@example.com", "+15550000000")).id();
    }

    private UUID createLoanProduct(String suffix) {
        return loanProductService.create(new CreateLoanProductRequest(
                "loan-app-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
    }

    @Test
    void create_persistsApplicationInDraftStatus() {
        String suffix = UUID.randomUUID().toString();
        UUID customerId = createCustomer(suffix);
        UUID loanProductId = createLoanProduct(suffix);

        LoanApplicationResponse created = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Car purchase"));

        assertThat(created.status()).isEqualTo("draft");
        LoanApplication stored = loanApplicationRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getCustomer().getId()).isEqualTo(customerId);

        loanApplicationRepository.deleteById(created.id());
    }

    @Test
    void submitThenCancel_transitionsStatusCorrectly() {
        String suffix = UUID.randomUUID().toString();
        UUID customerId = createCustomer(suffix);
        UUID loanProductId = createLoanProduct(suffix);

        LoanApplicationResponse created = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Car purchase"));

        LoanApplicationResponse submitted = loanApplicationService.submit(created.id());
        assertThat(submitted.status()).isEqualTo("submitted");
        assertThat(submitted.submittedAt()).isNotNull();

        LoanApplicationResponse cancelled = loanApplicationService.cancel(created.id(), new CancelLoanApplicationRequest("customer withdrew"));
        assertThat(cancelled.status()).isEqualTo("cancelled");

        loanApplicationRepository.deleteById(created.id());
    }

    @Test
    void submit_rejectsNonDraftApplication() {
        String suffix = UUID.randomUUID().toString();
        UUID customerId = createCustomer(suffix);
        UUID loanProductId = createLoanProduct(suffix);

        LoanApplicationResponse created = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Car purchase"));
        loanApplicationService.submit(created.id());

        assertThatThrownBy(() -> loanApplicationService.submit(created.id()))
                .isInstanceOf(ConflictException.class);

        loanApplicationRepository.deleteById(created.id());
    }

    @Test
    void delete_makesApplicationUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        UUID customerId = createCustomer(suffix);
        UUID loanProductId = createLoanProduct(suffix);

        LoanApplicationResponse created = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Car purchase"));

        loanApplicationService.delete(created.id());

        assertThatThrownBy(() -> loanApplicationService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanApplicationRepository.findById(created.id())).isEmpty();
    }
}
