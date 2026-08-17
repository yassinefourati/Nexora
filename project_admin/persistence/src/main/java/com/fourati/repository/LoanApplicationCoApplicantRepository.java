package com.fourati.repository;

import com.fourati.domain.LoanApplicationCoApplicant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationCoApplicantRepository extends JpaRepository<LoanApplicationCoApplicant, UUID> {

    List<LoanApplicationCoApplicant> findByLoanApplicationId(UUID loanApplicationId);

    boolean existsByLoanApplicationIdAndCustomerId(UUID loanApplicationId, UUID customerId);
}
