package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.domain.LoanProductEligibilityRule;
import com.fourati.dto.request.CreateLoanProductEligibilityRuleRequest;
import com.fourati.dto.response.LoanProductEligibilityRuleResponse;
import com.fourati.mapper.LoanProductEligibilityRuleMapper;
import com.fourati.repository.LoanProductEligibilityRuleRepository;
import com.fourati.repository.LoanProductRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanProductEligibilityRuleService {

    private final LoanProductEligibilityRuleRepository loanProductEligibilityRuleRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductEligibilityRuleMapper loanProductEligibilityRuleMapper;

    @Audited(action = "CREATE", description = "Add an eligibility rule to a loan product")
    public LoanProductEligibilityRuleResponse create(CreateLoanProductEligibilityRuleRequest request) {
        if (loanProductEligibilityRuleRepository.existsByLoanProductId(request.loanProductId())) {
            throw new ConflictException("Loan product " + request.loanProductId()
                    + " already has an eligibility rule");
        }
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));
        LoanProductEligibilityRule entity = loanProductEligibilityRuleMapper.toEntity(request);
        entity.setLoanProduct(loanProduct);
        LoanProductEligibilityRule saved = loanProductEligibilityRuleRepository.save(entity);
        return loanProductEligibilityRuleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanProductEligibilityRuleResponse> findByLoanProductId(UUID loanProductId) {
        return loanProductEligibilityRuleRepository.findByLoanProductId(loanProductId).stream()
                .map(loanProductEligibilityRuleMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an eligibility rule from a loan product")
    public void delete(UUID id) {
        LoanProductEligibilityRule entity = loanProductEligibilityRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProductEligibilityRule", id));
        loanProductEligibilityRuleRepository.delete(entity);
    }
}
