package com.fourati.service;

import com.fourati.domain.CollectionCase;
import com.fourati.domain.CollectionCaseStatusHistory;
import com.fourati.domain.LoanAccount;
import com.fourati.domain.LoanInstallment;
import com.fourati.dto.request.CreateCollectionCaseRequest;
import com.fourati.dto.request.EscalateCollectionCaseRequest;
import com.fourati.dto.request.ResolveCollectionCaseRequest;
import com.fourati.dto.request.WriteOffCollectionCaseRequest;
import com.fourati.dto.response.CollectionCaseResponse;
import com.fourati.mapper.CollectionCaseMapper;
import com.fourati.repository.CollectionCaseRepository;
import com.fourati.repository.CollectionCaseStatusHistoryRepository;
import com.fourati.repository.LoanAccountRepository;
import com.fourati.repository.LoanInstallmentRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Collection cases are explicit-state today (no workflow engine yet);
 * every transition is recorded in {@link CollectionCaseStatusHistory} so
 * the trail survives a future migration to Camunda-driven orchestration. A
 * case can only be opened against an unpaid {@link LoanInstallment} whose
 * due date has passed, belonging to the given {@link LoanAccount}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CollectionCaseService {

    private final CollectionCaseRepository collectionCaseRepository;
    private final CollectionCaseStatusHistoryRepository collectionCaseStatusHistoryRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CollectionCaseMapper collectionCaseMapper;

    @Audited(action = "CREATE", description = "Open a new collection case for an overdue installment")
    public CollectionCaseResponse create(CreateCollectionCaseRequest request) {
        if (collectionCaseRepository.existsByLoanInstallmentId(request.loanInstallmentId())) {
            throw new ConflictException("A collection case already exists for loan installment " + request.loanInstallmentId());
        }
        LoanAccount loanAccount = loanAccountRepository.findById(request.loanAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanAccount", request.loanAccountId()));
        LoanInstallment loanInstallment = loanInstallmentRepository.findById(request.loanInstallmentId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanInstallment", request.loanInstallmentId()));
        if (!loanInstallment.getLoanAccount().getId().equals(loanAccount.getId())) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId()
                    + " does not belong to loan account " + request.loanAccountId());
        }
        if ("paid".equals(loanInstallment.getStatus())) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId() + " is already paid");
        }
        if (!loanInstallment.getDueDate().isBefore(LocalDate.now())) {
            throw new ConflictException("Loan installment " + request.loanInstallmentId() + " is not yet overdue");
        }

        CollectionCase entity = collectionCaseMapper.toEntity(request);
        entity.setLoanAccount(loanAccount);
        entity.setLoanInstallment(loanInstallment);
        entity.setOverdueAmount(loanInstallment.getTotalAmount());
        CollectionCase saved = collectionCaseRepository.save(entity);
        recordStatusChange(saved, null, "open", "Collection case opened");
        return collectionCaseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CollectionCaseResponse findById(UUID id) {
        return collectionCaseMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CollectionCaseResponse> findAll(Pageable pageable) {
        return collectionCaseRepository.findAll(pageable).map(collectionCaseMapper::toResponse);
    }

    @Audited(action = "ESCALATE", description = "Escalate a collection case to the next stage")
    public CollectionCaseResponse escalate(UUID id, EscalateCollectionCaseRequest request) {
        CollectionCase entity = getEntityOrThrow(id);
        if ("resolved".equals(entity.getStatus()) || "written_off".equals(entity.getStatus())) {
            throw new ConflictException("Collection case " + id + " is already closed, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStage(request.stage());
        entity.setStatus("in_progress");
        CollectionCase saved = collectionCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "in_progress", "Escalated to stage: " + request.stage());
        return collectionCaseMapper.toResponse(saved);
    }

    @Audited(action = "RESOLVE", description = "Resolve a collection case")
    public CollectionCaseResponse resolve(UUID id, ResolveCollectionCaseRequest request) {
        CollectionCase entity = getEntityOrThrow(id);
        if ("resolved".equals(entity.getStatus()) || "written_off".equals(entity.getStatus())) {
            throw new ConflictException("Collection case " + id + " is already closed, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("resolved");
        entity.setResolutionNotes(request.resolutionNotes());
        entity.setResolvedAt(Instant.now());
        CollectionCase saved = collectionCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "resolved", request.resolutionNotes());
        return collectionCaseMapper.toResponse(saved);
    }

    @Audited(action = "WRITE_OFF", description = "Write off a collection case")
    public CollectionCaseResponse writeOff(UUID id, WriteOffCollectionCaseRequest request) {
        CollectionCase entity = getEntityOrThrow(id);
        if ("resolved".equals(entity.getStatus()) || "written_off".equals(entity.getStatus())) {
            throw new ConflictException("Collection case " + id + " is already closed, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("written_off");
        entity.setResolutionNotes(request.resolutionNotes());
        entity.setResolvedAt(Instant.now());
        CollectionCase saved = collectionCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "written_off", request.resolutionNotes());
        return collectionCaseMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a collection case")
    public void delete(UUID id) {
        CollectionCase entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        collectionCaseRepository.save(entity);
    }

    private void recordStatusChange(CollectionCase collectionCase, String fromStatus, String toStatus, String reason) {
        CollectionCaseStatusHistory history = new CollectionCaseStatusHistory();
        history.setCollectionCase(collectionCase);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        collectionCaseStatusHistoryRepository.save(history);
    }

    private CollectionCase getEntityOrThrow(UUID id) {
        return collectionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CollectionCase", id));
    }
}
