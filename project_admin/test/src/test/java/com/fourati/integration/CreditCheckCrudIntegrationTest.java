package com.fourati.integration;

import com.fourati.dto.request.CreateCreditCheckRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.response.CreditCheckResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.repository.CreditCheckRepository;
import com.fourati.service.CreditCheckService;
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
 * covering the credit check create -> process flow through the real
 * service -> repository -> database path (not mocks), using the mock
 * credit bureau client wired as the real Spring bean.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class CreditCheckCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CreditCheckService creditCheckService;

    @Autowired
    private CreditCheckRepository creditCheckRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    @Autowired
    private LoanApplicationService loanApplicationService;

    private UUID createLoanApplication(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "credit-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "credit-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        return loanApplicationService.create(new CreateLoanApplicationRequest(
                customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Test")).id();
    }

    @Test
    void createThenProcess_completesCreditCheckWithAssessment() {
        UUID loanApplicationId = createLoanApplication(UUID.randomUUID().toString());
        UUID customerId = loanApplicationService.findById(loanApplicationId).customerId();

        CreditCheckResponse created = creditCheckService.create(new CreateCreditCheckRequest(loanApplicationId, customerId));
        assertThat(created.status()).isEqualTo("pending");

        CreditCheckResponse processed = creditCheckService.process(created.id());
        assertThat(processed.status()).isEqualTo("completed");
        assertThat(processed.completedAt()).isNotNull();

        creditCheckRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsSecondCreditCheckForSameApplication() {
        UUID loanApplicationId = createLoanApplication(UUID.randomUUID().toString());
        UUID customerId = loanApplicationService.findById(loanApplicationId).customerId();

        CreditCheckResponse first = creditCheckService.create(new CreateCreditCheckRequest(loanApplicationId, customerId));

        assertThatThrownBy(() -> creditCheckService.create(new CreateCreditCheckRequest(loanApplicationId, customerId)))
                .isInstanceOf(ConflictException.class);

        creditCheckRepository.deleteById(first.id());
    }
}
