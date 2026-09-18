package com.fourati.service;

import com.fourati.dto.response.LoanRepaymentStatusHistoryResponse;
import com.fourati.mapper.LoanRepaymentStatusHistoryMapper;
import com.fourati.repository.LoanRepaymentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanRepaymentService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanRepaymentStatusHistoryService {

    private final LoanRepaymentStatusHistoryRepository loanRepaymentStatusHistoryRepository;
    private final LoanRepaymentStatusHistoryMapper loanRepaymentStatusHistoryMapper;

    public List<LoanRepaymentStatusHistoryResponse> findByLoanRepaymentId(UUID loanRepaymentId) {
        return loanRepaymentStatusHistoryRepository.findByLoanRepaymentIdOrderByChangedAtAsc(loanRepaymentId).stream()
                .map(loanRepaymentStatusHistoryMapper::toResponse)
                .toList();
    }
}
