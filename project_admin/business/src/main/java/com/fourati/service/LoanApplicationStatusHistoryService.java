package com.fourati.service;

import com.fourati.dto.response.LoanApplicationStatusHistoryResponse;
import com.fourati.mapper.LoanApplicationStatusHistoryMapper;
import com.fourati.repository.LoanApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanApplicationService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanApplicationStatusHistoryService {

    private final LoanApplicationStatusHistoryRepository loanApplicationStatusHistoryRepository;
    private final LoanApplicationStatusHistoryMapper loanApplicationStatusHistoryMapper;

    public List<LoanApplicationStatusHistoryResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return loanApplicationStatusHistoryRepository.findByLoanApplicationIdOrderByChangedAtAsc(loanApplicationId).stream()
                .map(loanApplicationStatusHistoryMapper::toResponse)
                .toList();
    }
}
