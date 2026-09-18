package com.fourati.integration;

import com.fourati.domain.KycCase;
import com.fourati.dto.request.CompleteKycCaseRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateKycCaseRequest;
import com.fourati.dto.response.KycCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.repository.KycCaseRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.KycCaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering the KYC case open -> start review -> complete flow through the
 * real service -> repository -> database path (not mocks).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class KycCaseCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private KycCaseService kycCaseService;

    @Autowired
    private KycCaseRepository kycCaseRepository;

    @Autowired
    private CustomerService customerService;

    private UUID createCustomer(String suffix) {
        return customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "kyc-test-" + suffix + "@example.com", "+15550000000")).id();
    }

    @Test
    void create_opensCaseInPendingStatus() {
        UUID customerId = createCustomer(UUID.randomUUID().toString());

        KycCaseResponse created = kycCaseService.create(new CreateKycCaseRequest(customerId));

        assertThat(created.status()).isEqualTo("pending");
        KycCase stored = kycCaseRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getCustomer().getId()).isEqualTo(customerId);

        kycCaseRepository.deleteById(created.id());
    }

    @Test
    void startReviewThenComplete_transitionsStatusCorrectly() {
        UUID customerId = createCustomer(UUID.randomUUID().toString());
        KycCaseResponse created = kycCaseService.create(new CreateKycCaseRequest(customerId));

        KycCaseResponse inProgress = kycCaseService.startReview(created.id());
        assertThat(inProgress.status()).isEqualTo("in_progress");

        KycCaseResponse completed = kycCaseService.complete(created.id(), new CompleteKycCaseRequest("passed", "all checks clear"));
        assertThat(completed.status()).isEqualTo("passed");
        assertThat(completed.completedAt()).isNotNull();

        kycCaseRepository.deleteById(created.id());
    }

    @Test
    void complete_rejectsInvalidOutcome() {
        UUID customerId = createCustomer(UUID.randomUUID().toString());
        KycCaseResponse created = kycCaseService.create(new CreateKycCaseRequest(customerId));

        assertThatThrownBy(() -> kycCaseService.complete(created.id(), new CompleteKycCaseRequest("bogus", null)))
                .isInstanceOf(ConflictException.class);

        kycCaseRepository.deleteById(created.id());
    }
}
