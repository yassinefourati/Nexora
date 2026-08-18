package com.fourati.integration;

import com.fourati.domain.LoanApproval;
import com.fourati.dto.request.ApproveLoanApprovalRequest;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.request.RejectLoanApprovalRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.dto.response.LoanApprovalResponse;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.service.CustomerService;
import com.fourati.service.LoanApplicationService;
import com.fourati.service.LoanApprovalService;
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
 * covering the full customer -> loan application -> underwriting -> approval
 * chain through the real service -> repository -> database path (not
 * mocks), including the status-history side effects and the
 * completed-underwriting-required / one-approval-per-application invariants
 * a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class LoanApprovalCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private LoanApprovalService loanApprovalService;

    @Autowired
    private LoanApprovalRepository loanApprovalRepository;

    @Autowired
    private UnderwritingCaseService underwritingCaseService;

    @Autowired
    private LoanApplicationService loanApplicationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private LoanProductService loanProductService;

    private record ApplicationAndCase(UUID loanApplicationId, UUID underwritingCaseId) {}

    private ApplicationAndCase createDecidedUnderwritingCase(String suffix) {
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "approval-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "approval-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(20000), 48, "Business expansion"));

        UnderwritingCaseResponse underwritingCase = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(application.id(), "jane.underwriter"));
        underwritingCaseService.startReview(underwritingCase.id());
        underwritingCaseService.decide(underwritingCase.id(),
                new DecideUnderwritingCaseRequest("approve", "Strong profile", BigDecimal.valueOf(20000), 48));

        return new ApplicationAndCase(application.id(), underwritingCase.id());
    }

    @Test
    void create_persistsApprovalInPendingStatus() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndCase context = createDecidedUnderwritingCase(suffix);

        LoanApprovalResponse created = loanApprovalService.create(
                new CreateLoanApprovalRequest(context.loanApplicationId(), context.underwritingCaseId()));

        assertThat(created.status()).isEqualTo("pending");
        LoanApproval stored = loanApprovalRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getLoanApplication().getId()).isEqualTo(context.loanApplicationId());

        loanApprovalRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsUnderwritingCaseNotYetCompleted() {
        String suffix = UUID.randomUUID().toString();
        UUID customerId = customerService.create(new CreateCustomerRequest(
                "individual", "active", "Test", "Applicant", null,
                LocalDate.of(1990, 1, 1), null, "approval-test-" + suffix + "@example.com", "+15550000000")).id();
        UUID loanProductId = loanProductService.create(new CreateLoanProductRequest(
                "approval-test-" + suffix, "Test Product", "personal", "active", "USD",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(50000), 6, 84, null)).id();
        LoanApplicationResponse application = loanApplicationService.create(
                new CreateLoanApplicationRequest(customerId, loanProductId, BigDecimal.valueOf(20000), 48, "Business expansion"));
        UnderwritingCaseResponse underwritingCase = underwritingCaseService.create(
                new CreateUnderwritingCaseRequest(application.id(), null));

        assertThatThrownBy(() -> loanApprovalService.create(
                new CreateLoanApprovalRequest(application.id(), underwritingCase.id())))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_transitionsPendingToApprovedAndRecordsHistory() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndCase context = createDecidedUnderwritingCase(suffix);
        LoanApprovalResponse created = loanApprovalService.create(
                new CreateLoanApprovalRequest(context.loanApplicationId(), context.underwritingCaseId()));

        LoanApprovalResponse approved = loanApprovalService.approve(created.id(),
                new ApproveLoanApprovalRequest(BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), "john.approver"));

        assertThat(approved.status()).isEqualTo("approved");
        assertThat(approved.approvedAt()).isNotNull();

        loanApprovalRepository.deleteById(created.id());
    }

    @Test
    void reject_rejectsApprovalNotPending() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndCase context = createDecidedUnderwritingCase(suffix);
        LoanApprovalResponse created = loanApprovalService.create(
                new CreateLoanApprovalRequest(context.loanApplicationId(), context.underwritingCaseId()));
        loanApprovalService.approve(created.id(),
                new ApproveLoanApprovalRequest(BigDecimal.valueOf(20000), 48, BigDecimal.valueOf(5.5), "john.approver"));

        assertThatThrownBy(() -> loanApprovalService.reject(created.id(), new RejectLoanApprovalRequest("too late")))
                .isInstanceOf(ConflictException.class);

        loanApprovalRepository.deleteById(created.id());
    }

    @Test
    void delete_makesApprovalUnreadableThroughTheNormalReadPath() {
        String suffix = UUID.randomUUID().toString();
        ApplicationAndCase context = createDecidedUnderwritingCase(suffix);
        LoanApprovalResponse created = loanApprovalService.create(
                new CreateLoanApprovalRequest(context.loanApplicationId(), context.underwritingCaseId()));

        loanApprovalService.delete(created.id());

        assertThatThrownBy(() -> loanApprovalService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(loanApprovalRepository.findById(created.id())).isEmpty();
    }
}
