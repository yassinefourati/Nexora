package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.domain.LoanProductVersion;
import com.fourati.dto.request.CreateLoanProductVersionRequest;
import com.fourati.dto.response.LoanProductVersionResponse;
import com.fourati.mapper.LoanProductVersionMapper;
import com.fourati.repository.LoanProductRepository;
import com.fourati.repository.LoanProductVersionRepository;
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
public class LoanProductVersionService {

    private final LoanProductVersionRepository loanProductVersionRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanProductVersionMapper loanProductVersionMapper;

    @Audited(action = "CREATE", description = "Add a version to a loan product")
    public LoanProductVersionResponse create(CreateLoanProductVersionRequest request) {
        if (loanProductVersionRepository.existsByLoanProductIdAndVersionNumber(request.loanProductId(), request.versionNumber())) {
            throw new ConflictException("Loan product " + request.loanProductId()
                    + " already has version " + request.versionNumber());
        }
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));
        LoanProductVersion entity = loanProductVersionMapper.toEntity(request);
        entity.setLoanProduct(loanProduct);
        LoanProductVersion saved = loanProductVersionRepository.save(entity);
        return loanProductVersionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LoanProductVersionResponse> findByLoanProductId(UUID loanProductId) {
        return loanProductVersionRepository.findByLoanProductId(loanProductId).stream()
                .map(loanProductVersionMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a version from a loan product")
    public void delete(UUID id) {
        LoanProductVersion entity = loanProductVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProductVersion", id));
        loanProductVersionRepository.delete(entity);
    }
}
