package com.fourati.repository;

import com.fourati.domain.CreditCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CreditCheckRepository extends JpaRepository<CreditCheck, UUID>, JpaSpecificationExecutor<CreditCheck> {

    boolean existsByLoanApplicationId(UUID loanApplicationId);

    Optional<CreditCheck> findByLoanApplicationId(UUID loanApplicationId);
}
