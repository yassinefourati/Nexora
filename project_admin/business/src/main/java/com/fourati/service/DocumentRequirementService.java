package com.fourati.service;

import com.fourati.domain.DocumentRequirement;
import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CreateDocumentRequirementRequest;
import com.fourati.dto.response.DocumentRequirementResponse;
import com.fourati.mapper.DocumentRequirementMapper;
import com.fourati.repository.DocumentRequirementRepository;
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
public class DocumentRequirementService {

    private final DocumentRequirementRepository documentRequirementRepository;
    private final LoanProductRepository loanProductRepository;
    private final DocumentRequirementMapper documentRequirementMapper;

    @Audited(action = "CREATE", description = "Add a document requirement to a loan product")
    public DocumentRequirementResponse create(CreateDocumentRequirementRequest request) {
        if (documentRequirementRepository.existsByLoanProductIdAndDocumentType(request.loanProductId(), request.documentType())) {
            throw new ConflictException("Loan product " + request.loanProductId()
                    + " already requires document type " + request.documentType());
        }
        LoanProduct loanProduct = loanProductRepository.findById(request.loanProductId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanProduct", request.loanProductId()));
        DocumentRequirement entity = documentRequirementMapper.toEntity(request);
        entity.setLoanProduct(loanProduct);
        DocumentRequirement saved = documentRequirementRepository.save(entity);
        return documentRequirementMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DocumentRequirementResponse> findByLoanProductId(UUID loanProductId) {
        return documentRequirementRepository.findByLoanProductId(loanProductId).stream()
                .map(documentRequirementMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a document requirement from a loan product")
    public void delete(UUID id) {
        DocumentRequirement entity = documentRequirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DocumentRequirement", id));
        documentRequirementRepository.delete(entity);
    }
}
