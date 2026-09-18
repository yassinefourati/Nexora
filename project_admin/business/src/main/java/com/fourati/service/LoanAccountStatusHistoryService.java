package com.fourati.service;

import com.fourati.dto.response.LoanAccountStatusHistoryResponse;
import com.fourati.mapper.LoanAccountStatusHistoryMapper;
import com.fourati.repository.LoanAccountStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanAccountService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanAccountStatusHistoryService {

    private final LoanAccountStatusHistoryRepository loanAccountStatusHistoryRepository;
    private final LoanAccountStatusHistoryMapper loanAccountStatusHistoryMapper;

    public List<LoanAccountStatusHistoryResponse> findByLoanAccountId(UUID loanAccountId) {
        return loanAccountStatusHistoryRepository.findByLoanAccountIdOrderByChangedAtAsc(loanAccountId).stream()
                .map(loanAccountStatusHistoryMapper::toResponse)
                .toList();
    }
}
