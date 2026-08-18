package com.fourati.service;

import com.fourati.dto.response.LoanContractStatusHistoryResponse;
import com.fourati.mapper.LoanContractStatusHistoryMapper;
import com.fourati.repository.LoanContractStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanContractService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanContractStatusHistoryService {

    private final LoanContractStatusHistoryRepository loanContractStatusHistoryRepository;
    private final LoanContractStatusHistoryMapper loanContractStatusHistoryMapper;

    public List<LoanContractStatusHistoryResponse> findByLoanContractId(UUID loanContractId) {
        return loanContractStatusHistoryRepository.findByLoanContractIdOrderByChangedAtAsc(loanContractId).stream()
                .map(loanContractStatusHistoryMapper::toResponse)
                .toList();
    }
}
