package com.fourati.service;

import com.fourati.dto.response.LoanDisbursementStatusHistoryResponse;
import com.fourati.mapper.LoanDisbursementStatusHistoryMapper;
import com.fourati.repository.LoanDisbursementStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanDisbursementService}
 * on every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanDisbursementStatusHistoryService {

    private final LoanDisbursementStatusHistoryRepository loanDisbursementStatusHistoryRepository;
    private final LoanDisbursementStatusHistoryMapper loanDisbursementStatusHistoryMapper;

    public List<LoanDisbursementStatusHistoryResponse> findByLoanDisbursementId(UUID loanDisbursementId) {
        return loanDisbursementStatusHistoryRepository.findByLoanDisbursementIdOrderByChangedAtAsc(loanDisbursementId).stream()
                .map(loanDisbursementStatusHistoryMapper::toResponse)
                .toList();
    }
}
