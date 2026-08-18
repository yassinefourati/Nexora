package com.fourati.repository;

import com.fourati.domain.LoanContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanContractRepository extends JpaRepository<LoanContract, UUID> {

    Optional<LoanContract> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);

    boolean existsByContractNumber(String contractNumber);
}
