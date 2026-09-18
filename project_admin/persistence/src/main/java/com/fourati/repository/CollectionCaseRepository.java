package com.fourati.repository;

import com.fourati.domain.CollectionCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectionCaseRepository extends JpaRepository<CollectionCase, UUID> {

    boolean existsByLoanInstallmentId(UUID loanInstallmentId);
}
