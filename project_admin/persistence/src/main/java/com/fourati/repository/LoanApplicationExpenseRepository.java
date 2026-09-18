package com.fourati.repository;

import com.fourati.domain.LoanApplicationExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationExpenseRepository extends JpaRepository<LoanApplicationExpense, UUID> {

    List<LoanApplicationExpense> findByLoanApplicationId(UUID loanApplicationId);
}
