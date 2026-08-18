package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.domain.LoanProductFeeRule;
import com.fourati.dto.request.CreateLoanProductFeeRuleRequest;
import com.fourati.dto.response.LoanProductFeeRuleResponse;
import com.fourati.mapper.LoanProductFeeRuleMapper;
import com.fourati.repository.LoanProductFeeRuleRepository;
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
public class LoanProductFeeRuleService {

    private final LoanProductFeeRuleRepository loanProductFeeRuleRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductFeeRuleMapper loanProductFeeRuleMapper;

    @Audited(action = "CREATE", description = "Add a fee rule to a loan product")
    public LoanProductFeeRuleResponse create(CreateLoanProductFeeRuleRequest request) {
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));
        LoanProductFeeRule entity = loanProductFeeRuleMapper.toEntity(request);
        entity.setLoanProduct(loanProduct);
        LoanProductFeeRule saved = loanProductFeeRuleRepository.save(entity);
        return loanProductFeeRuleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanProductFeeRuleResponse> findByLoanProductId(UUID loanProductId) {
        return loanProductFeeRuleRepository.findByLoanProductId(loanProductId).stream()
                .map(loanProductFeeRuleMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a fee rule from a loan product")
    public void delete(UUID id) {
        LoanProductFeeRule entity = loanProductFeeRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProductFeeRule", id));
        loanProductFeeRuleRepository.delete(entity);
    }
}
