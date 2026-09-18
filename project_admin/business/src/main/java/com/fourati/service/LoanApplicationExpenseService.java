package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApplicationExpense;
import com.fourati.dto.request.CreateLoanApplicationExpenseRequest;
import com.fourati.dto.response.LoanApplicationExpenseResponse;
import com.fourati.mapper.LoanApplicationExpenseMapper;
import com.fourati.repository.LoanApplicationExpenseRepository;
import com.fourati.repository.LoanApplicationRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanApplicationExpenseService {

    private final LoanApplicationExpenseRepository loanApplicationExpenseRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationExpenseMapper loanApplicationExpenseMapper;

    @Audited(action = "CREATE", description = "Add an expense to a loan application")
    public LoanApplicationExpenseResponse create(CreateLoanApplicationExpenseRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanApplicationExpense entity = loanApplicationExpenseMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        LoanApplicationExpense saved = loanApplicationExpenseRepository.save(entity);
        return loanApplicationExpenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationExpenseResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return loanApplicationExpenseRepository.findByLoanApplicationId(loanApplicationId).stream()
                .map(loanApplicationExpenseMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an expense from a loan application")
    public void delete(UUID id) {
        LoanApplicationExpense entity = loanApplicationExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplicationExpense", id));
        loanApplicationExpenseRepository.delete(entity);
    }
}
