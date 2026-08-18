package com.fourati.integration;

import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanNotificationRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanNotificationResponse;
import com.fourati.repository.LoanNotificationRepository;
import com.fourati.repository.NotificationRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanNotificationService;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering sending a loan notification through the real service ->
 * repository -> database path (not mocks), asserting both the underlying
 * platform notification and the loan_notifications link row are persisted.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanNotificationCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanNotificationService loanNotificationService;

    @Autowired
    private LoanNotificationRepository loanNotificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    private UUID createLoanApplication(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "loan-notification-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "loan-notification-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(5000), 12, "Car purchase"));
        return application.id();
    }

    @Test
    void create_persistsNotificationAndLinksItToApplication() {
        String suffix = UUID.randomUUID().toString();
        UUID loanApplicationId = createLoanApplication(suffix);

        LoanNotificationResponse created = loanNotificationService.create(new CreateLoanNotificationRequest(
                loanApplicationId, "application_submitted", "Application submitted",
                "Your loan application has been submitted for review.", "email"));

        assertThat(created.eventType()).isEqualTo("application_submitted");
        assertThat(notificationRepository.findById(created.notificationId())).isPresent();
        assertThat(loanNotificationRepository.findById(created.id())).isPresent();

        List<LoanNotificationResponse> forApplication = loanNotificationService.findByLoanApplicationId(loanApplicationId);
        assertThat(forApplication).extracting(LoanNotificationResponse::id).contains(created.id());

        loanNotificationRepository.deleteById(created.id());
        notificationRepository.deleteById(created.notificationId());
    }
}
