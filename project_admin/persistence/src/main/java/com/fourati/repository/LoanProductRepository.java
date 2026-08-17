package com.fourati.repository;

import com.fourati.domain.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID>, JpaSpecificationExecutor<LoanProduct> {

    boolean existsByCode(String code);

    Optional<LoanProduct> findByCode(String code);
}
