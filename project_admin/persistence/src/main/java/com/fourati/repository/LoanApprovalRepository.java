package com.fourati.repository;

import com.fourati.domain.LoanApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanApprovalRepository extends JpaRepository<LoanApproval, UUID> {

    Optional<LoanApproval> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);
}
