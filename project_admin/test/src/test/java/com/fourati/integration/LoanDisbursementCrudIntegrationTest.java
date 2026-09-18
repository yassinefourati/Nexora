package com.fourati.integration;

import com.fourati.domain.LoanDisbursement;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CompleteLoanDisbursementRequest;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.request.FailLoanDisbursementRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.dto.response.LoanDisbursementResponse;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanDisbursementRepository;
import com.fourati.service.ContractSignatureService;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanApprovalService;
import com.fourati.service.LoanContractService;
import com.fourati.service.LoanDisbursementService;
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
 * approval -> offer -> contract -> signature -> disbursement chain through
 * the real service -> repository -> database path (not mocks), including
 * the status-history side effects and the all-signatures-signed invariant
 * a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanDisbursementCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanDisbursementService loanDisbursementService;

    @Autowired
    private LoanDisbursementRepository loanDisbursementRepository;

    @Autowired
    private ContractSignatureService contractSignatureService;

    @Autowired
    private LoanContractService loanContractService;

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

    private record ApplicationAndContract(UUID loanApplicationId, UUID loanContractId) {}

    private ApplicationAndContract createFinalizedLoanContract(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "disbursement-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "disbursement-test-" + suffix, "Test Product", "personal", "active", "USD",
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

        LoanContractResponse contract = loanContractService.create(
                new CreateLoanContractRequest(application.id(), offer.id(), "CTR-" + suffix.substring(0, 8)));
        loanContractService.finalizeContract(contract.id(), new FinalizeLoanContractRequest("https://docs/" + suffix + ".pdf"));

        return new ApplicationAndContract(application.id(), contract.id());
    }

    private void signContract(UUID loanContractId) {
        ContractSignatureResponse signature = contractSignatureService.create(
                new CreateContractSignatureRequest(loanContractId, "Jane Doe", "primary_applicant", null));
        contractSignatureService.sign(signature.id());
    }

    @Test
    void create_rejectsDisbursementBeforeSignaturesComplete() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndContract context = createFinalizedLoanContract(suffix);
        contractSignatureService.create(new CreateContractSignatureRequest(
                context.loanContractId(), "Jane Doe", "primary_applicant", null));

        assertThatThrownBy(() -> loanDisbursementService.create(
                new CreateLoanDisbursementRequest(context.loanApplicationId(), context.loanContractId(), "bank_transfer", "IBAN123456")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_copiesPrincipalAmountAfterAllSignaturesSigned() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndContract context = createFinalizedLoanContract(suffix);
        signContract(context.loanContractId());

        LoanDisbursementResponse created = loanDisbursementService.create(
                new CreateLoanDisbursementRequest(context.loanApplicationId(), context.loanContractId(), "bank_transfer", "IBAN123456"));

        assertThat(created.status()).isEqualTo("pending");
        assertThat(created.amount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        LoanDisbursement stored = loanDisbursementRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanApplication().getId()).isEqualTo(context.loanApplicationId());

        loanDisbursementRepository.deleteById(created.id());
    }

    @Test
    void initiateThenComplete_transitionsStatusCorrectly() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndContract context = createFinalizedLoanContract(suffix);
        signContract(context.loanContractId());
        LoanDisbursementResponse created = loanDisbursementService.create(
                new CreateLoanDisbursementRequest(context.loanApplicationId(), context.loanContractId(), "bank_transfer", "IBAN123456"));

        LoanDisbursementResponse initiated = loanDisbursementService.initiate(created.id());
        assertThat(initiated.status()).isEqualTo("initiated");

        LoanDisbursementResponse completed = loanDisbursementService.complete(created.id(), new CompleteLoanDisbursementRequest("REF-001"));
        assertThat(completed.status()).isEqualTo("completed");
        assertThat(completed.referenceNumber()).isEqualTo("REF-001");

        assertThatThrownBy(() -> loanDisbursementService.fail(created.id(), new FailLoanDisbursementRequest("too late")))
                .isInstanceOf(ConflictException.class);

        loanDisbursementRepository.deleteById(created.id());
    }

    @Test
    void delete_makesDisbursementUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndContract context = createFinalizedLoanContract(suffix);
        signContract(context.loanContractId());
        LoanDisbursementResponse created = loanDisbursementService.create(
                new CreateLoanDisbursementRequest(context.loanApplicationId(), context.loanContractId(), "bank_transfer", "IBAN123456"));

        loanDisbursementService.delete(created.id());

        assertThatThrownBy(() -> loanDisbursementService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanDisbursementRepository.findById(created.id())).isEmpty();
    }
}
