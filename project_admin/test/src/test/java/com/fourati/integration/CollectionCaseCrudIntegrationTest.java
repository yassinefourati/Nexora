package com.fourati.integration;

import com.fourati.domain.CollectionCase;
import com.fourati.domain.LoanInstallment;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CompleteLoanDisbursementRequest;
import com.fourati.dto.request.CreateCollectionCaseRequest;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanAccountRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.request.EscalateCollectionCaseRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.request.ResolveCollectionCaseRequest;
import com.fourati.dto.response.CollectionCaseResponse;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.dto.response.LoanAccountResponse;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.dto.response.LoanDisbursementResponse;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CollectionCaseRepository;
import com.fourati.repository.LoanInstallmentRepository;
import com.fourati.service.CollectionCaseService;
import com.fourati.service.ContractSignatureService;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanAccountService;
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
 * covering the full customer -> ... -> loan account -> collection case
 * chain through the real service -> repository -> database path (not
 * mocks). Since a freshly generated installment schedule is never overdue
 * at creation time, the first installment's due date is backdated directly
 * via the repository to simulate an overdue installment before opening the
 * collection case.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class CollectionCaseCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CollectionCaseService collectionCaseService;

    @Autowired
    private CollectionCaseRepository collectionCaseRepository;

    @Autowired
    private LoanInstallmentRepository loanInstallmentRepository;

    @Autowired
    private LoanAccountService loanAccountService;

    @Autowired
    private LoanDisbursementService loanDisbursementService;

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

    private record AccountAndOverdueInstallment(UUID loanAccountId, UUID loanInstallmentId) {}

    private AccountAndOverdueInstallment createAccountWithOverdueInstallment(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "collections-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "collections-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(12000), 12, "Auto loan"));

        UnderwritingCaseResponse underwritingCase = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(application.id(), "jane.underwriter"));
        underwritingCaseService.startReview(underwritingCase.id());
        underwritingCaseService.decide(underwritingCase.id(),
                new DecideUnderwritingCaseRequest("approve", "Strong profile", BigDecimal.valueOf(12000), 12));

        LoanApprovalResponse approval = loanApprovalService.create(
                new CreateLoanApprovalRequest(application.id(), underwritingCase.id()));
        loanApprovalService.approve(approval.id(),
                new ApproveLoanApprovalRequest(BigDecimal.valueOf(12000), 12, BigDecimal.valueOf(6), "john.approver"));

        LoanOfferResponse offer = loanOfferService.create(new CreateLoanOfferRequest(
                application.id(), approval.id(), Instant.now().plus(30, ChronoUnit.DAYS)));
        loanOfferService.accept(offer.id());

        LoanContractResponse contract = loanContractService.create(
                new CreateLoanContractRequest(application.id(), offer.id(), "CTR-" + suffix.substring(0, 8)));
        loanContractService.finalizeContract(contract.id(), new FinalizeLoanContractRequest("https://docs/" + suffix + ".pdf"));

        ContractSignatureResponse signature = contractSignatureService.create(
                new CreateContractSignatureRequest(contract.id(), "Jane Doe", "primary_applicant", null));
        contractSignatureService.sign(signature.id());

        LoanDisbursementResponse disbursement = loanDisbursementService.create(
                new CreateLoanDisbursementRequest(application.id(), contract.id(), "bank_transfer", "IBAN123456"));
        loanDisbursementService.initiate(disbursement.id());
        loanDisbursementService.complete(disbursement.id(), new CompleteLoanDisbursementRequest("REF-001"));

        LoanAccountResponse account = loanAccountService.create(
                new CreateLoanAccountRequest(application.id(), disbursement.id(), "ACC-" + suffix.substring(0, 8)));

        LoanInstallment firstInstallment = loanInstallmentRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(account.id()).get(0);
        firstInstallment.setDueDate(LocalDate.now().minusDays(10));
        loanInstallmentRepository.save(firstInstallment);

        return new AccountAndOverdueInstallment(account.id(), firstInstallment.getId());
    }

    @Test
    void create_persistsCaseInOpenStatus() {
        String suffix = UUID.randomUUID().toString();
        AccountAndOverdueInstallment context = createAccountWithOverdueInstallment(suffix);

        CollectionCaseResponse created = collectionCaseService.create(
                new CreateCollectionCaseRequest(context.loanAccountId(), context.loanInstallmentId(), "agent.smith"));

        assertThat(created.status()).isEqualTo("open");
        CollectionCase stored = collectionCaseRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanAccount().getId()).isEqualTo(context.loanAccountId());

        collectionCaseRepository.deleteById(created.id());
    }

    @Test
    void escalateThenResolve_transitionsStatusCorrectly() {
        String suffix = UUID.randomUUID().toString();
        AccountAndOverdueInstallment context = createAccountWithOverdueInstallment(suffix);
        CollectionCaseResponse created = collectionCaseService.create(
                new CreateCollectionCaseRequest(context.loanAccountId(), context.loanInstallmentId(), "agent.smith"));

        CollectionCaseResponse escalated = collectionCaseService.escalate(created.id(), new EscalateCollectionCaseRequest("notice"));
        assertThat(escalated.status()).isEqualTo("in_progress");
        assertThat(escalated.stage()).isEqualTo("notice");

        CollectionCaseResponse resolved = collectionCaseService.resolve(created.id(), new ResolveCollectionCaseRequest("customer paid overdue amount"));
        assertThat(resolved.status()).isEqualTo("resolved");
        assertThat(resolved.resolvedAt()).isNotNull();

        assertThatThrownBy(() -> collectionCaseService.escalate(created.id(), new EscalateCollectionCaseRequest("agency")))
                .isInstanceOf(ConflictException.class);

        collectionCaseRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsInstallmentNotYetOverdue() {
        String suffix = UUID.randomUUID().toString();
        AccountAndOverdueInstallment context = createAccountWithOverdueInstallment(suffix);
        LoanInstallment secondInstallment = loanInstallmentRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(context.loanAccountId()).get(1);

        assertThatThrownBy(() -> collectionCaseService.create(
                new CreateCollectionCaseRequest(context.loanAccountId(), secondInstallment.getId(), null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        assertThatThrownBy(() -> collectionCaseService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
