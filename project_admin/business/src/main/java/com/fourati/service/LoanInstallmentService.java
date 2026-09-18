package com.fourati.service;

import com.fourati.dto.response.LoanInstallmentResponse;
import com.fourati.mapper.LoanInstallmentMapper;
import com.fourati.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Read-only: rows are written internally by {@link LoanAccountService} when
 * an account's repayment schedule is generated. Payment capture against
 * installments (marking them paid/overdue) belongs to a later Repayment
 * module, not this one.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanInstallmentService {

    private final LoanInstallmentRepository loanInstallmentRepository;
    private final LoanInstallmentMapper loanInstallmentMapper;

    public List<LoanInstallmentResponse> findByLoanAccountId(UUID loanAccountId) {
        return loanInstallmentRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccountId).stream()
                .map(loanInstallmentMapper::toResponse)
                .toList();
    }
}
