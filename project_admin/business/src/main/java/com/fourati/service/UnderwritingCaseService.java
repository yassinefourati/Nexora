package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.UnderwritingCase;
import com.fourati.domain.UnderwritingStatusHistory;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.mapper.UnderwritingCaseMapper;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.UnderwritingCaseRepository;
import com.fourati.repository.UnderwritingStatusHistoryRepository;
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
 * Underwriting decisions are explicit-state today (no workflow engine yet);
 * every transition is recorded in {@link UnderwritingStatusHistory} so the
 * trail survives a future migration to Camunda-driven orchestration. This
 * service deliberately reads only {@link LoanApplication} — credit, risk and
 * fraud outcomes are looked up independently by an underwriter through their
 * own modules rather than being coupled to here.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UnderwritingCaseService {

    private final UnderwritingCaseRepository underwritingCaseRepository;
    private final UnderwritingStatusHistoryRepository underwritingStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UnderwritingCaseMapper underwritingCaseMapper;

    @Audited(action = "CREATE", description = "Open a new underwriting case")
    public UnderwritingCaseResponse create(CreateUnderwritingCaseRequest request) {
        if (underwritingCaseRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("An underwriting case already exists for loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));

        UnderwritingCase entity = underwritingCaseMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        UnderwritingCase saved = underwritingCaseRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Underwriting case opened");
        return underwritingCaseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UnderwritingCaseResponse findById(UUID id) {
        return underwritingCaseMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<UnderwritingCaseResponse> findAll(Pageable pageable) {
        return underwritingCaseRepository.findAll(pageable).map(underwritingCaseMapper::toResponse);
    }

    @Audited(action = "START_REVIEW", description = "Start reviewing an underwriting case")
    public UnderwritingCaseResponse startReview(UUID id) {
        UnderwritingCase entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Underwriting case " + id + " must be pending to start review, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("in_review");
        UnderwritingCase saved = underwritingCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "in_review", "Review started");
        return underwritingCaseMapper.toResponse(saved);
    }

    @Audited(action = "DECIDE", description = "Record the underwriting decision for a case")
    public UnderwritingCaseResponse decide(UUID id, DecideUnderwritingCaseRequest request) {
        UnderwritingCase entity = getEntityOrThrow(id);
        if (!"in_review".equals(entity.getStatus())) {
            throw new ConflictException("Underwriting case " + id + " must be in_review to decide, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setDecision(request.decision());
        entity.setDecisionReason(request.decisionReason());
        entity.setApprovedAmount(request.approvedAmount());
        entity.setApprovedTermMonths(request.approvedTermMonths());
        entity.setDecidedAt(Instant.now());
        entity.setStatus("completed");
        UnderwritingCase saved = underwritingCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "completed", "Decision recorded: " + request.decision());
        return underwritingCaseMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete an underwriting case")
    public void delete(UUID id) {
        UnderwritingCase entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        underwritingCaseRepository.save(entity);
    }

    private void recordStatusChange(UnderwritingCase underwritingCase, String fromStatus, String toStatus, String reason) {
        UnderwritingStatusHistory history = new UnderwritingStatusHistory();
        history.setUnderwritingCase(underwritingCase);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        underwritingStatusHistoryRepository.save(history);
    }

    private UnderwritingCase getEntityOrThrow(UUID id) {
        return underwritingCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UnderwritingCase", id));
    }
}
