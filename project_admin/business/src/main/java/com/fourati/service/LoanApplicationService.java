package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApplicationStatusHistory;
import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CancelLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.UpdateLoanApplicationRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.mapper.LoanApplicationMapper;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.repository.LoanApplicationStatusHistoryRepository;
import com.fourati.repository.LoanProductRepository;
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
 * Application status transitions are explicit-state today (no workflow
 * engine yet); every transition is recorded in
 * {@link LoanApplicationStatusHistory} so the trail survives a future
 * migration to Camunda-driven orchestration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStatusHistoryRepository loanApplicationStatusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationMapper loanApplicationMapper;

    @Audited(action = "CREATE", description = "Create a new loan application")
    public LoanApplicationResponse create(CreateLoanApplicationRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));

        LoanApplication entity = loanApplicationMapper.toEntity(request);
        entity.setCustomer(customer);
        entity.setLoanProduct(loanProduct);
        LoanApplication saved = loanApplicationRepository.save(entity);
        recordStatusChange(saved, null, "draft", "Application created");
        return loanApplicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse findById(UUID id) {
        return loanApplicationMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> findAll(Pageable pageable) {
        return loanApplicationRepository.findAll(pageable).map(loanApplicationMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a loan application")
    public LoanApplicationResponse update(UUID id, UpdateLoanApplicationRequest request) {
        LoanApplication entity = getEntityOrThrow(id);
        if (!"draft".equals(entity.getStatus())) {
            throw new ConflictException("Loan application " + id + " can only be edited while in draft status");
        }
        loanApplicationMapper.updateEntityFromRequest(request, entity);
        LoanApplication saved = loanApplicationRepository.save(entity);
        return loanApplicationMapper.toResponse(saved);
    }

    @Audited(action = "SUBMIT", description = "Submit a loan application")
    public LoanApplicationResponse submit(UUID id) {
        LoanApplication entity = getEntityOrThrow(id);
        if (!"draft".equals(entity.getStatus())) {
            throw new ConflictException("Loan application " + id + " must be in draft status to submit, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("submitted");
        entity.setSubmittedAt(Instant.now());
        LoanApplication saved = loanApplicationRepository.save(entity);
        recordStatusChange(saved, previousStatus, "submitted", "Application submitted");
        return loanApplicationMapper.toResponse(saved);
    }

    @Audited(action = "CANCEL", description = "Cancel a loan application")
    public LoanApplicationResponse cancel(UUID id, CancelLoanApplicationRequest request) {
        LoanApplication entity = getEntityOrThrow(id);
        if ("approved".equals(entity.getStatus()) || "rejected".equals(entity.getStatus())
                || "cancelled".equals(entity.getStatus()) || "withdrawn".equals(entity.getStatus())) {
            throw new ConflictException("Loan application " + id + " cannot be cancelled from status: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("cancelled");
        LoanApplication saved = loanApplicationRepository.save(entity);
        recordStatusChange(saved, previousStatus, "cancelled", request.reason());
        return loanApplicationMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan application")
    public void delete(UUID id) {
        LoanApplication entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanApplicationRepository.save(entity);
    }

    private void recordStatusChange(LoanApplication application, String fromStatus, String toStatus, String reason) {
        LoanApplicationStatusHistory history = new LoanApplicationStatusHistory();
        history.setLoanApplication(application);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        loanApplicationStatusHistoryRepository.save(history);
    }

    private LoanApplication getEntityOrThrow(UUID id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", id));
    }
}
