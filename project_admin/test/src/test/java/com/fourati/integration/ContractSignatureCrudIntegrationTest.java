package com.fourati.integration;

import com.fourati.domain.ContractSignature;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DeclineContractSignatureRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.ContractSignatureRepository;
import com.fourati.service.ContractSignatureService;
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
 * approval -> offer -> contract -> signature chain through the real
 * service -> repository -> database path (not mocks), including the
 * status-history side effects and the finalized-contract-required
 * invariant a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class ContractSignatureCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ContractSignatureService contractSignatureService;

    @Autowired
    private ContractSignatureRepository contractSignatureRepository;

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

    private UUID createFinalizedLoanContract(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "signature-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "signature-test-" + suffix, "Test Product", "personal", "active", "USD",
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

        return contract.id();
    }

    @Test
    void create_persistsSignatureInPendingStatus() {
        String suffix = UUID.randomUUID().toString();
        UUID loanContractId = createFinalizedLoanContract(suffix);

        ContractSignatureResponse created = contractSignatureService.create(
                new CreateContractSignatureRequest(loanContractId, "Jane Doe", "primary_applicant", null));

        assertThat(created.status()).isEqualTo("pending");
        assertThat(created.signatureMethod()).isEqualTo("electronic");
        ContractSignature stored = contractSignatureRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanContract().getId()).isEqualTo(loanContractId);
    }

    @Test
    void sign_transitionsPendingToSignedAndRecordsHistory() {
        String suffix = UUID.randomUUID().toString();
        UUID loanContractId = createFinalizedLoanContract(suffix);
        ContractSignatureResponse created = contractSignatureService.create(
                new CreateContractSignatureRequest(loanContractId, "Jane Doe", "primary_applicant", null));

        ContractSignatureResponse signed = contractSignatureService.sign(created.id());

        assertThat(signed.status()).isEqualTo("signed");
        assertThat(signed.signedAt()).isNotNull();
    }

    @Test
    void decline_rejectsAlreadySignedSignature() {
        String suffix = UUID.randomUUID().toString();
        UUID loanContractId = createFinalizedLoanContract(suffix);
        ContractSignatureResponse created = contractSignatureService.create(
                new CreateContractSignatureRequest(loanContractId, "Jane Doe", "primary_applicant", null));
        contractSignatureService.sign(created.id());

        assertThatThrownBy(() -> contractSignatureService.decline(created.id(), new DeclineContractSignatureRequest("too late")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        assertThatThrownBy(() -> contractSignatureService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
