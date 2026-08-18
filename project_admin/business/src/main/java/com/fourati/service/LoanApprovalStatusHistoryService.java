package com.fourati.service;

import com.fourati.dto.response.LoanApprovalStatusHistoryResponse;
import com.fourati.mapper.LoanApprovalStatusHistoryMapper;
import com.fourati.repository.LoanApprovalStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanApprovalService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanApprovalStatusHistoryService {

    private final LoanApprovalStatusHistoryRepository loanApprovalStatusHistoryRepository;
    private final LoanApprovalStatusHistoryMapper loanApprovalStatusHistoryMapper;

    public List<LoanApprovalStatusHistoryResponse> findByLoanApprovalId(UUID loanApprovalId) {
        return loanApprovalStatusHistoryRepository.findByLoanApprovalIdOrderByChangedAtAsc(loanApprovalId).stream()
                .map(loanApprovalStatusHistoryMapper::toResponse)
                .toList();
    }
}
