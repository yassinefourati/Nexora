package com.fourati.service;

import com.fourati.dto.response.LoanOfferStatusHistoryResponse;
import com.fourati.mapper.LoanOfferStatusHistoryMapper;
import com.fourati.repository.LoanOfferStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanOfferService} on
 * every status transition, not created through this API.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanOfferStatusHistoryService {

    private final LoanOfferStatusHistoryRepository loanOfferStatusHistoryRepository;
    private final LoanOfferStatusHistoryMapper loanOfferStatusHistoryMapper;

    public List<LoanOfferStatusHistoryResponse> findByLoanOfferId(UUID loanOfferId) {
        return loanOfferStatusHistoryRepository.findByLoanOfferIdOrderByChangedAtAsc(loanOfferId).stream()
                .map(loanOfferStatusHistoryMapper::toResponse)
                .toList();
    }
}
