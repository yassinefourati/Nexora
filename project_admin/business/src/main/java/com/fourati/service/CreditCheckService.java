package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CreditAssessment;
import com.fourati.domain.CreditCheck;
import com.fourati.domain.CreditCheckStatusHistory;
import com.fourati.domain.CreditReport;
import com.fourati.domain.CreditScore;
import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CreateCreditCheckRequest;
import com.fourati.dto.response.CreditCheckResponse;
import com.fourati.integration.credit.CreditBureauClient;
import com.fourati.integration.credit.CreditBureauReportRequest;
import com.fourati.integration.credit.CreditBureauReportResponse;
import com.fourati.mapper.CreditCheckMapper;
import com.fourati.repository.CreditAssessmentRepository;
import com.fourati.repository.CreditCheckRepository;
import com.fourati.repository.CreditCheckStatusHistoryRepository;
import com.fourati.repository.CreditReportRepository;
import com.fourati.repository.CreditScoreRepository;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Status transitions are explicit-state (no workflow engine yet, same
 * approach as LoanApplication/KycCase); every transition is recorded in
 * {@link CreditCheckStatusHistory}. Never calls the credit bureau
 * directly — always through {@link CreditBureauClient}, so tests and local
 * dev don't need real bureau connectivity.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreditCheckService {

    private final CreditCheckRepository creditCheckRepository;
    private final CreditReportRepository creditReportRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final CreditAssessmentRepository creditAssessmentRepository;
    private final CreditCheckStatusHistoryRepository creditCheckStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final CreditCheckMapper creditCheckMapper;
    private final CreditBureauClient creditBureauClient;

    @Audited(action = "CREATE", description = "Request a credit check for a loan application")
    public CreditCheckResponse create(CreateCreditCheckRequest request) {
        if (creditCheckRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("Loan application " + request.loanApplicationId() + " already has a credit check");
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        CreditCheck entity = new CreditCheck();
        entity.setLoanApplication(loanApplication);
        entity.setCustomer(customer);
        CreditCheck saved = creditCheckRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Credit check requested");
        return creditCheckMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CreditCheckResponse findById(UUID id) {
        return creditCheckMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CreditCheckResponse> findAll(Pageable pageable) {
        return creditCheckRepository.findAll(pageable).map(creditCheckMapper::toResponse);
    }

    @Audited(action = "PROCESS", description = "Retrieve the credit report and assess a credit check")
    public CreditCheckResponse process(UUID id) {
        CreditCheck entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Credit check " + id + " must be pending to process, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("in_progress");
        creditCheckRepository.save(entity);
        recordStatusChange(entity, previousStatus, "in_progress", "Requesting bureau report");

        CreditBureauReportResponse bureauResponse = creditBureauClient.getCreditReport(
                new CreditBureauReportRequest(entity.getCustomer().getId(), null));

        CreditReport report = new CreditReport();
        report.setCreditCheck(entity);
        report.setBureauName(bureauResponse.bureauName());
        report.setReportReference(bureauResponse.reportReference());
        report.setRawScore(bureauResponse.rawScore());
        creditReportRepository.save(report);

        CreditScore score = new CreditScore();
        score.setCreditCheck(entity);
        score.setScore(bureauResponse.normalizedScore());
        score.setScoreModel(bureauResponse.scoreModel());
        creditScoreRepository.save(score);

        CreditAssessment assessment = new CreditAssessment();
        assessment.setCreditCheck(entity);
        assessment.setDebtToIncomeRatio(bureauResponse.debtToIncomeRatio());
        assessment.setDecision(decisionFor(bureauResponse.normalizedScore()));
        creditAssessmentRepository.save(assessment);

        entity.setStatus("completed");
        entity.setCompletedAt(Instant.now());
        CreditCheck saved = creditCheckRepository.save(entity);
        recordStatusChange(saved, "in_progress", "completed", "Assessment: " + assessment.getDecision());

        return creditCheckMapper.toResponse(saved);
    }

    private String decisionFor(int score) {
        if (score >= 650) {
            return "approve";
        }
        if (score >= 580) {
            return "refer";
        }
        return "reject";
    }

    private void recordStatusChange(CreditCheck creditCheck, String fromStatus, String toStatus, String reason) {
        CreditCheckStatusHistory history = new CreditCheckStatusHistory();
        history.setCreditCheck(creditCheck);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        creditCheckStatusHistoryRepository.save(history);
    }

    private CreditCheck getEntityOrThrow(UUID id) {
        return creditCheckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CreditCheck", id));
    }
}
