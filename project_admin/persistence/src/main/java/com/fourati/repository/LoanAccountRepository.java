package com.fourati.repository;

import com.fourati.domain.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, UUID> {

    Optional<LoanAccount> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationId(UUID loanApplicationId);

    boolean existsByAccountNumber(String accountNumber);
}
