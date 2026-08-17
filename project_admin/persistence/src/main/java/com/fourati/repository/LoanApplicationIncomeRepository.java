package com.fourati.repository;

import com.fourati.domain.LoanApplicationIncome;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationIncomeRepository extends JpaRepository<LoanApplicationIncome, UUID> {

    List<LoanApplicationIncome> findByLoanApplicationId(UUID loanApplicationId);
}
