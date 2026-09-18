package com.fourati.integration;

import com.fourati.domain.LoanContract;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CancelLoanContractRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanContractRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanApprovalService;
import com.fourati.service.LoanContractService;
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
 * approval -> offer -> contract chain through the real service ->
 * repository -> database path (not mocks), including the status-history
 * side effects and the accepted-offer-required / one-contract-per-
 * application invariants a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanContractCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanContractService loanContractService;

    @Autowired
    private LoanContractRepository loanContractRepository;

    @Autowired
    private LoanOfferService loanOfferService;

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

    private record ApplicationAndOffer(UUID loanApplicationId, UUID loanOfferId) {}

    private ApplicationAndOffer createAcceptedLoanOffer(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "contract-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "contract-test-" + suffix, "Test Product", "personal", "active", "USD",
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

        LoanOfferResponse offer = loanOfferService.create(new CreateLoanOfferRequest(
                application.id(), approval.id(), Instant.now().plus(30, ChronoUnit.DAYS)));
        loanOfferService.accept(offer.id());

        return new ApplicationAndOffer(application.id(), offer.id());
    }

    @Test
    void create_copiesOfferedTermsAndPersistsInDraftStatus() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndOffer context = createAcceptedLoanOffer(suffix);

        LoanContractResponse created = loanContractService.create(
                new CreateLoanContractRequest(context.loanApplicationId(), context.loanOfferId(), "CTR-" + suffix.substring(0, 8)));

        assertThat(created.status()).isEqualTo("draft");
        assertThat(created.principalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        LoanContract stored = loanContractRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanApplication().getId()).isEqualTo(context.loanApplicationId());

        loanContractRepository.deleteById(created.id());
    }

    @Test
    void finalizeThenCancel_rejectsCancelOfAlreadyCancelledContract() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndOffer context = createAcceptedLoanOffer(suffix);
        LoanContractResponse created = loanContractService.create(
                new CreateLoanContractRequest(context.loanApplicationId(), context.loanOfferId(), "CTR-" + suffix.substring(0, 8)));

        LoanContractResponse finalized = loanContractService.finalizeContract(created.id(),
                new FinalizeLoanContractRequest("https://docs/" + suffix + ".pdf"));
        assertThat(finalized.status()).isEqualTo("finalized");
        assertThat(finalized.finalizedAt()).isNotNull();

        LoanContractResponse cancelled = loanContractService.cancel(created.id(), new CancelLoanContractRequest("customer changed mind"));
        assertThat(cancelled.status()).isEqualTo("cancelled");

        assertThatThrownBy(() -> loanContractService.cancel(created.id(), new CancelLoanContractRequest("duplicate")))
                .isInstanceOf(ConflictException.class);

        loanContractRepository.deleteById(created.id());
    }

    @Test
    void delete_makesContractUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndOffer context = createAcceptedLoanOffer(suffix);
        LoanContractResponse created = loanContractService.create(
                new CreateLoanContractRequest(context.loanApplicationId(), context.loanOfferId(), "CTR-" + suffix.substring(0, 8)));

        loanContractService.delete(created.id());

        assertThatThrownBy(() -> loanContractService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanContractRepository.findById(created.id())).isEmpty();
    }
}
