package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanContract;
import com.fourati.domain.LoanDisbursement;
import com.fourati.domain.LoanDisbursementStatusHistory;
import com.fourati.dto.request.CompleteLoanDisbursementRequest;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.request.FailLoanDisbursementRequest;
import com.fourati.dto.response.LoanDisbursementResponse;
import com.fourati.mapper.LoanDisbursementMapper;
import com.fourati.repository.ContractSignatureRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanContractRepository;
import com.fourati.repository.LoanDisbursementRepository;
import com.fourati.repository.LoanDisbursementStatusHistoryRepository;
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
 * Disbursements are explicit-state today (no workflow engine yet); every
 * transition is recorded in {@link LoanDisbursementStatusHistory} so the
 * trail survives a future migration to Camunda-driven orchestration. A
 * disbursement can only be created once every {@link
 * com.fourati.domain.ContractSignature} on the referenced {@link
 * LoanContract} is signed — checked directly via the signature repository
 * rather than coupling this entity to the Signature module's tables.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanDisbursementService {

    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanDisbursementStatusHistoryRepository loanDisbursementStatusHistoryRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanContractRepository loanContractRepository;
    private final ContractSignatureRepository contractSignatureRepository;
    private final LoanDisbursementMapper loanDisbursementMapper;

    @Audited(action = "CREATE", description = "Create a new loan disbursement")
    public LoanDisbursementResponse create(CreateLoanDisbursementRequest request) {
        if (loanDisbursementRepository.existsByLoanApplicationId(request.loanApplicationId())) {
            throw new ConflictException("A loan disbursement already exists for loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanContract loanContract = loanContractRepository.findById(request.loanContractId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanContract", request.loanContractId()));
        if (!"finalized".equals(loanContract.getStatus())) {
            throw new ConflictException("Loan contract " + request.loanContractId()
                    + " must be finalized before a disbursement can be created");
        }
        var signatures = contractSignatureRepository.findByLoanContractId(request.loanContractId());
        if (signatures.isEmpty() || signatures.stream().anyMatch(s -> !"signed".equals(s.getStatus()))) {
            throw new ConflictException("Loan contract " + request.loanContractId()
                    + " must have all required signatures signed before a disbursement can be created");
        }

        LoanDisbursement entity = loanDisbursementMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setLoanContract(loanContract);
        entity.setAmount(loanContract.getPrincipalAmount());
        LoanDisbursement saved = loanDisbursementRepository.save(entity);
        recordStatusChange(saved, null, "pending", "Disbursement created");
        return loanDisbursementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanDisbursementResponse findById(UUID id) {
        return loanDisbursementMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanDisbursementResponse> findAll(Pageable pageable) {
        return loanDisbursementRepository.findAll(pageable).map(loanDisbursementMapper::toResponse);
    }

    @Audited(action = "INITIATE", description = "Initiate a loan disbursement")
    public LoanDisbursementResponse initiate(UUID id) {
        LoanDisbursement entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan disbursement " + id + " must be pending to initiate, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("initiated");
        entity.setInitiatedAt(Instant.now());
        LoanDisbursement saved = loanDisbursementRepository.save(entity);
        recordStatusChange(saved, previousStatus, "initiated", "Disbursement initiated");
        return loanDisbursementMapper.toResponse(saved);
    }

    @Audited(action = "COMPLETE", description = "Complete a loan disbursement")
    public LoanDisbursementResponse complete(UUID id, CompleteLoanDisbursementRequest request) {
        LoanDisbursement entity = getEntityOrThrow(id);
        if (!"initiated".equals(entity.getStatus())) {
            throw new ConflictException("Loan disbursement " + id + " must be initiated to complete, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setReferenceNumber(request.referenceNumber());
        entity.setCompletedAt(Instant.now());
        entity.setStatus("completed");
        LoanDisbursement saved = loanDisbursementRepository.save(entity);
        recordStatusChange(saved, previousStatus, "completed", "Disbursement completed, reference: " + request.referenceNumber());
        return loanDisbursementMapper.toResponse(saved);
    }

    @Audited(action = "FAIL", description = "Mark a loan disbursement as failed")
    public LoanDisbursementResponse fail(UUID id, FailLoanDisbursementRequest request) {
        LoanDisbursement entity = getEntityOrThrow(id);
        if (!"initiated".equals(entity.getStatus())) {
            throw new ConflictException("Loan disbursement " + id + " must be initiated to fail, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setFailureReason(request.failureReason());
        entity.setFailedAt(Instant.now());
        entity.setStatus("failed");
        LoanDisbursement saved = loanDisbursementRepository.save(entity);
        recordStatusChange(saved, previousStatus, "failed", request.failureReason());
        return loanDisbursementMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan disbursement")
    public void delete(UUID id) {
        LoanDisbursement entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanDisbursementRepository.save(entity);
    }

    private void recordStatusChange(LoanDisbursement loanDisbursement, String fromStatus, String toStatus, String reason) {
        LoanDisbursementStatusHistory history = new LoanDisbursementStatusHistory();
        history.setLoanDisbursement(loanDisbursement);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanDisbursementStatusHistoryRepository.save(history);
    }

    private LoanDisbursement getEntityOrThrow(UUID id) {
        return loanDisbursementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanDisbursement", id));
    }
}
