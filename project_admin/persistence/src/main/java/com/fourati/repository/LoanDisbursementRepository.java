package com.fourati.repository;

import com.fourati.domain.LoanDisbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, UUID> {

    Optional<LoanDisbursement> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
