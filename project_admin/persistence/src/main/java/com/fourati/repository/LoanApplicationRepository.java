package com.fourati.repository;

import com.fourati.domain.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID>, JpaSpecificationExecutor<LoanApplication> {
}
