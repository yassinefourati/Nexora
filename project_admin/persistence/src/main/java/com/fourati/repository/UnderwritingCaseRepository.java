package com.fourati.repository;

import com.fourati.domain.UnderwritingCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UnderwritingCaseRepository extends JpaRepository<UnderwritingCase, UUID> {

    Optional<UnderwritingCase> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
