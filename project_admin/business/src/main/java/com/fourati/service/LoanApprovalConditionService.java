package com.fourati.service;

import com.fourati.domain.LoanApproval;
import com.fourati.domain.LoanApprovalCondition;
import com.fourati.dto.request.CreateLoanApprovalConditionRequest;
import com.fourati.dto.response.LoanApprovalConditionResponse;
import com.fourati.mapper.LoanApprovalConditionMapper;
import com.fourati.repository.LoanApprovalConditionRepository;
import com.fourati.repository.LoanApprovalRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanApprovalConditionService {

    private final LoanApprovalConditionRepository loanApprovalConditionRepository;
    private final LoanApprovalRepository loanApprovalRepository;
    private final LoanApprovalConditionMapper loanApprovalConditionMapper;

    @Audited(action = "CREATE", description = "Attach a condition to a loan approval")
    public LoanApprovalConditionResponse create(CreateLoanApprovalConditionRequest request) {
        LoanApproval loanApproval = loanApprovalRepository.findById(request.loanApprovalId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApproval", request.loanApprovalId()));

        LoanApprovalCondition entity = loanApprovalConditionMapper.toEntity(request);
        entity.setLoanApproval(loanApproval);
        LoanApprovalCondition saved = loanApprovalConditionRepository.save(entity);
        return loanApprovalConditionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanApprovalConditionResponse> findByLoanApprovalId(UUID loanApprovalId) {
        return loanApprovalConditionRepository.findByLoanApprovalId(loanApprovalId).stream()
                .map(loanApprovalConditionMapper::toResponse)
                .toList();
    }

    @Audited(action = "SATISFY", description = "Mark a loan approval condition as satisfied")
    public LoanApprovalConditionResponse satisfy(UUID id) {
        LoanApprovalCondition entity = getEntityOrThrow(id);
        if (!"pending".equals(entity.getStatus())) {
            throw new ConflictException("Loan approval condition " + id + " must be pending to satisfy, was: " + entity.getStatus());
        }
        entity.setStatus("satisfied");
        entity.setSatisfiedAt(Instant.now());
        LoanApprovalCondition saved = loanApprovalConditionRepository.save(entity);
        return loanApprovalConditionMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Remove a condition from a loan approval")
    public void delete(UUID id) {
        LoanApprovalCondition entity = getEntityOrThrow(id);
        loanApprovalConditionRepository.delete(entity);
    }

    private LoanApprovalCondition getEntityOrThrow(UUID id) {
        return loanApprovalConditionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApprovalCondition", id));
    }
}
