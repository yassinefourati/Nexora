package com.fourati.service;

import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.UpdateLoanProductRequest;
import com.fourati.dto.response.LoanProductResponse;
import com.fourati.mapper.LoanProductMapper;
import com.fourati.repository.LoanProductRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanProductService {

    private final LoanProductRepository loanProductRepository;
    private final LoanProductMapper loanProductMapper;

    @Audited(action = "CREATE", description = "Create a new loan product")
    public LoanProductResponse create(CreateLoanProductRequest request) {
        if (loanProductRepository.existsByCode(request.code())) {
            throw new ConflictException("Loan product already exists with code: " + request.code());
        }
        LoanProduct entity = loanProductMapper.toEntity(request);
        LoanProduct saved = loanProductRepository.save(entity);
        return loanProductMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanProductResponse findById(UUID id) {
        return loanProductMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<LoanProductResponse> findAll(Pageable pageable) {
        return loanProductRepository.findAll(pageable).map(loanProductMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a loan product")
    public LoanProductResponse update(UUID id, UpdateLoanProductRequest request) {
        LoanProduct entity = getEntityOrThrow(id);
        if (!entity.getCode().equals(request.code()) && loanProductRepository.existsByCode(request.code())) {
            throw new ConflictException("Loan product already exists with code: " + request.code());
        }
        loanProductMapper.updateEntityFromRequest(request, entity);
        LoanProduct saved = loanProductRepository.save(entity);
        return loanProductMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a loan product")
    public void delete(UUID id) {
        LoanProduct entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        loanProductRepository.save(entity);
    }

    private LoanProduct getEntityOrThrow(UUID id) {
        return loanProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", id));
    }
}
