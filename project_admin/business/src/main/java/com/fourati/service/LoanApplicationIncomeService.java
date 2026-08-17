package com.fourati.service;

import com.fourati.domain.LoanApplication;
import com.fourati.domain.LoanApplicationIncome;
import com.fourati.dto.request.CreateLoanApplicationIncomeRequest;
import com.fourati.dto.response.LoanApplicationIncomeResponse;
import com.fourati.mapper.LoanApplicationIncomeMapper;
import com.fourati.repository.LoanApplicationIncomeRepository;
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
public class LoanApplicationIncomeService {

    private final LoanApplicationIncomeRepository loanApplicationIncomeRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationIncomeMapper loanApplicationIncomeMapper;

    @Audited(action = "CREATE", description = "Add an income source to a loan application")
    public LoanApplicationIncomeResponse create(CreateLoanApplicationIncomeRequest request) {
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        LoanApplicationIncome entity = loanApplicationIncomeMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        LoanApplicationIncome saved = loanApplicationIncomeRepository.save(entity);
        return loanApplicationIncomeMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationIncomeResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return loanApplicationIncomeRepository.findByLoanApplicationId(loanApplicationId).stream()
                .map(loanApplicationIncomeMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an income source from a loan application")
    public void delete(UUID id) {
        LoanApplicationIncome entity = loanApplicationIncomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplicationIncome", id));
        loanApplicationIncomeRepository.delete(entity);
    }
}
