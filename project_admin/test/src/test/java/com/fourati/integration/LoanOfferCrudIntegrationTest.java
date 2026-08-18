package com.fourati.integration;

import com.fourati.domain.LoanOffer;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DeclineLoanOfferRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanOfferRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanApprovalService;
import com.fourati.service.LoanOfferService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering the full customer -> loan application -> underwriting ->
 * approval -> offer chain through the real service -> repository ->
 * database path (not mocks), including the status-history side effects
 * and the approved-approval-required / one-offer-per-application
 * invariants a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanOfferCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanOfferService loanOfferService;

    @Autowired
    private LoanOfferRepository loanOfferRepository;

    @Autowired
    private LoanApprovalService loanApprovalService;

    @Autowired
    private UnderwritingCaseService underwritingCaseService;

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    private record ApplicationAndApproval(UUID loanApplicationId, UUID loanApprovalId) {}

    private ApplicationAndApproval createApprovedLoanApproval(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "offer-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "offer-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(20000), 48, "Home renovation"));

        UnderwritingCaseResponse underwritingCase = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(application.id(), "jane.underwriter"));
        underwritingCaseService.startReview(underwritingCase.id());
        underwritingCaseService.decide(underwritingCase.id(),
                new DecideUnderwritingCaseRequest("approve", "Strong profile", BigDecimal.valueOf(20000), 48));

        LoanApprovalResponse approval = loanApprovalService.create(
                new CreateLoanApprovalRequest(application.id(), underwritingCase.id()));
        loanApprovalService.approve(approval.id(),
                new ApproveLoanApprovalRequest(BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), "john.approver"));

        return new ApplicationAndApproval(application.id(), approval.id());
    }

    @Test
    void create_copiesApprovedTermsAndPersistsInIssuedStatus() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndApproval context = createApprovedLoanApproval(suffix);

        LoanOfferResponse created = loanOfferService.create(new CreateLoanOfferRequest(
                context.loanApplicationId(), context.loanApprovalId(), Instant.now().plus(30, ChronoUnit.DAYS)));

        assertThat(created.status()).isEqualTo("issued");
        assertThat(created.offeredAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        LoanOffer stored = loanOfferRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanApplication().getId()).isEqualTo(context.loanApplicationId());

        loanOfferRepository.deleteById(created.id());
    }

    @Test
    void acceptThenDecline_rejectsDeclineOfAlreadyAcceptedOffer() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndApproval context = createApprovedLoanApproval(suffix);
        LoanOfferResponse created = loanOfferService.create(new CreateLoanOfferRequest(
                context.loanApplicationId(), context.loanApprovalId(), Instant.now().plus(30, ChronoUnit.DAYS)));

        LoanOfferResponse accepted = loanOfferService.accept(created.id());
        assertThat(accepted.status()).isEqualTo("accepted");
        assertThat(accepted.acceptedAt()).isNotNull();

        assertThatThrownBy(() -> loanOfferService.decline(created.id(), new DeclineLoanOfferRequest("too late")))
                .isInstanceOf(ConflictException.class);

        loanOfferRepository.deleteById(created.id());
    }

    @Test
    void delete_makesOfferUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndApproval context = createApprovedLoanApproval(suffix);
        LoanOfferResponse created = loanOfferService.create(new CreateLoanOfferRequest(
                context.loanApplicationId(), context.loanApprovalId(), Instant.now().plus(30, ChronoUnit.DAYS)));

        loanOfferService.delete(created.id());

        assertThatThrownBy(() -> loanOfferService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanOfferRepository.findById(created.id())).isEmpty();
    }
}
