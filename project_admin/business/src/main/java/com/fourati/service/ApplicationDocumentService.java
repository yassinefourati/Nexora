package com.fourati.service;

import com.fourati.domain.ApplicationDocument;
import com.fourati.domain.Document;
import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CreateApplicationDocumentRequest;
import com.fourati.dto.response.ApplicationDocumentResponse;
import com.fourati.mapper.ApplicationDocumentMapper;
import com.fourati.repository.ApplicationDocumentRepository;
import com.fourati.repository.DocumentRepository;
import com.fourati.repository.LoanApplicationRepository;
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
public class ApplicationDocumentService {

    private final ApplicationDocumentRepository applicationDocumentRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationDocumentMapper applicationDocumentMapper;

    @Audited(action = "CREATE", description = "Attach a document to a loan application")
    public ApplicationDocumentResponse create(CreateApplicationDocumentRequest request) {
        if (applicationDocumentRepository.existsByLoanApplicationIdAndDocumentId(
                request.loanApplicationId(), request.documentId())) {
            throw new ConflictException("Document " + request.documentId()
                    + " is already attached to loan application " + request.loanApplicationId());
        }
        LoanApplication loanApplication = loanApplicationRepository.findById(request.loanApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", request.loanApplicationId()));
        Document document = documentRepository.findById(request.documentId())
                .orElseThrow(() -> new ResourceNotFoundException("Document", request.documentId()));

        ApplicationDocument entity = applicationDocumentMapper.toEntity(request);
        entity.setLoanApplication(loanApplication);
        entity.setDocument(document);
        ApplicationDocument saved = applicationDocumentRepository.save(entity);
        return applicationDocumentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDocumentResponse> findByLoanApplicationId(UUID loanApplicationId) {
        return applicationDocumentRepository.findByLoanApplicationId(loanApplicationId).stream()
                .map(applicationDocumentMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Detach a document from a loan application")
    public void delete(UUID id) {
        ApplicationDocument entity = applicationDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApplicationDocument", id));
        applicationDocumentRepository.delete(entity);
    }
}
