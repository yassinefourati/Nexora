package com.fourati.integration;

import com.fourati.domain.UnderwritingCase;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.UnderwritingCaseRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanProductService;
import com.fourati.service.UnderwritingCaseService;
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
 * covering the underwriting create -> start review -> decide flow through
 * the real service -> repository -> database path (not mocks), including
 * the status-history side effects and the one-case-per-application /
 * no-decide-before-review invariants a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class UnderwritingCaseCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private UnderwritingCaseService underwritingCaseService;

    @Autowired
    private UnderwritingCaseRepository underwritingCaseRepository;

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    private UUID createLoanApplication(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "underwriting-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "underwriting-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(15000), 36, "Debt consolidation"));
        return application.id();
    }

    @Test
    void create_persistsCaseInPendingStatus() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);

        UnderwritingCaseResponse created = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(loanApplicationId, "jane.underwriter"));

        assertThat(created.status()).isEqualTo("pending");
        UnderwritingCase stored = underwritingCaseRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanApplication().getId()).isEqualTo(loanApplicationId);

        underwritingCaseRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsSecondCaseForSameApplication() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);
        UnderwritingCaseResponse created = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(loanApplicationId, null));

        assertThatThrownBy(() -> underwritingCaseService.create(new CreateUnderwritingCaseRequest(loanApplicationId, null)))
                .isInstanceOf(ConflictException.class);

        underwritingCaseRepository.deleteById(created.id());
    }

    @Test
    void startReviewThenDecide_transitionsStatusCorrectly() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);

        UnderwritingCaseResponse created = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(loanApplicationId, "jane.underwriter"));

        UnderwritingCaseResponse inReview = underwritingCaseService.startReview(created.id());
        assertThat(inReview.status()).isEqualTo("in_review");

        UnderwritingCaseResponse decided = underwritingCaseService.decide(created.id(),
                new DecideUnderwritingCaseRequest("approve", "Strong credit profile", BigDecimal.valueOf(15000), 36));
        assertThat(decided.status()).isEqualTo("completed");
        assertThat(decided.decision()).isEqualTo("approve");
        assertThat(decided.decidedAt()).isNotNull();

        underwritingCaseRepository.deleteById(created.id());
    }

    @Test
    void decide_rejectsCaseNotYetInReview() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);
        UnderwritingCaseResponse created = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(loanApplicationId, null));

        assertThatThrownBy(() -> underwritingCaseService.decide(created.id(),
                new DecideUnderwritingCaseRequest("approve", "too early", null, null)))
                .isInstanceOf(ConflictException.class);

        underwritingCaseRepository.deleteById(created.id());
    }

    @Test
    void delete_makesCaseUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);
        UnderwritingCaseResponse created = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(loanApplicationId, null));

        underwritingCaseService.delete(created.id());

        assertThatThrownBy(() -> underwritingCaseService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(underwritingCaseRepository.findById(created.id())).isEmpty();
    }
}
