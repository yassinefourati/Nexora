package com.fourati.repository;

import com.fourati.domain.FraudCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID>, JpaSpecificationExecutor<FraudCheck> {

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
