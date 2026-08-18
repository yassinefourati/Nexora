package com.fourati.repository;

import com.fourati.domain.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, UUID> {

    List<LoanInstallment> findByLoanAccountIdOrderByInstallmentNumberAsc(UUID loanAccountId);
}
