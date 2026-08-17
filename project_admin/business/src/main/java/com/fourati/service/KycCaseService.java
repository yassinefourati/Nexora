package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.KycCase;
import com.fourati.domain.KycStatusHistory;
import com.fourati.dto.request.CompleteKycCaseRequest;
import com.fourati.dto.request.CreateKycCaseRequest;
import com.fourati.dto.response.KycCaseResponse;
import com.fourati.mapper.KycCaseMapper;
import com.fourati.repository.CustomerRepository;
import com.fourati.repository.KycCaseRepository;
import com.fourati.repository.KycStatusHistoryRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Status transitions are explicit-state today (no workflow engine yet, same
 * as LoanApplication); every transition is recorded in
 * {@link KycStatusHistory} so the trail survives a future migration to
 * Camunda-driven orchestration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class KycCaseService {

    private static final List<String> VALID_OUTCOMES = List.of("passed", "failed", "manual_review", "expired");

    private final KycCaseRepository kycCaseRepository;
    private final KycStatusHistoryRepository kycStatusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final KycCaseMapper kycCaseMapper;

    @Audited(action = "CREATE", description = "Open a new KYC case")
    public KycCaseResponse create(CreateKycCaseRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        KycCase entity = kycCaseMapper.toEntity(request);
        entity.setCustomer(customer);
        KycCase saved = kycCaseRepository.save(entity);
        recordStatusChange(saved, null, "pending", "KYC case opened");
        return kycCaseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public KycCaseResponse findById(UUID id) {
        return kycCaseMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<KycCaseResponse> findAll(Pageable pageable) {
        return kycCaseRepository.findAll(pageable).map(kycCaseMapper::toResponse);
    }

    @Audited(action = "START_REVIEW", description = "Move a KYC case into progress")
    public KycCaseResponse startReview(UUID id) {
        KycCase entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("KYC case " + id + " must be pending to start review, was: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus("in_progress");
        KycCase saved = kycCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, "in_progress", "Review started");
        return kycCaseMapper.toResponse(saved);
    }

    @Audited(action = "COMPLETE", description = "Complete a KYC case")
    public KycCaseResponse complete(UUID id, CompleteKycCaseRequest request) {
        if (!VALID_OUTCOMES.contains(request.outcome())) {
            throw new ConflictException("Invalid KYC case outcome: " + request.outcome());
        }
        KycCase entity = getEntityOrThrow(id);
        if (!"in_progress".equals(entity.getStatus()) && !"pending".equals(entity.getStatus())) {
            throw new ConflictException("KYC case " + id + " cannot be completed from status: " + entity.getStatus());
        }
        String previousStatus = entity.getStatus();
        entity.setStatus(request.outcome());
        entity.setCompletedAt(Instant.now());
        KycCase saved = kycCaseRepository.save(entity);
        recordStatusChange(saved, previousStatus, request.outcome(), request.reason());
        return kycCaseMapper.toResponse(saved);
    }

    private void recordStatusChange(KycCase kycCase, String fromStatus, String toStatus, String reason) {
        KycStatusHistory history = new KycStatusHistory();
        history.setKycCase(kycCase);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        kycStatusHistoryRepository.save(history);
    }

    private KycCase getEntityOrThrow(UUID id) {
        return kycCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KycCase", id));
    }
}
