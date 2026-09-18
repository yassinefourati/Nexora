package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.domain.LoanProductRateRule;
import com.fourati.dto.request.CreateLoanProductRateRuleRequest;
import com.fourati.dto.response.LoanProductRateRuleResponse;
import com.fourati.mapper.LoanProductRateRuleMapper;
import com.fourati.repository.LoanProductRateRuleRepository;
import com.fourati.repository.LoanProductRepository;
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
public class LoanProductRateRuleService {

    private final LoanProductRateRuleRepository loanProductRateRuleRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductRateRuleMapper loanProductRateRuleMapper;

    @Audited(action = "CREATE", description = "Add a rate rule to a loan product")
    public LoanProductRateRuleResponse create(CreateLoanProductRateRuleRequest request) {
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));
        LoanProductRateRule entity = loanProductRateRuleMapper.toEntity(request);
        entity.setLoanProduct(loanProduct);
        LoanProductRateRule saved = loanProductRateRuleRepository.save(entity);
        return loanProductRateRuleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanProductRateRuleResponse> findByLoanProductId(UUID loanProductId) {
        return loanProductRateRuleRepository.findByLoanProductId(loanProductId).stream()
                .map(loanProductRateRuleMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a rate rule from a loan product")
    public void delete(UUID id) {
        LoanProductRateRule entity = loanProductRateRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProductRateRule", id));
        loanProductRateRuleRepository.delete(entity);
    }
}
