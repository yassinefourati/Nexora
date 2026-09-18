package com.fourati.repository;

import com.fourati.domain.ApplicationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, UUID> {

    List<ApplicationDocument> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationIdAndDocumentId(UUID loanApplicationId, UUID documentId);
}
