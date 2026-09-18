package com.fourati.repository;

import com.fourati.domain.CreditAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditAssessmentRepository extends JpaRepository<CreditAssessment, UUID> {

    List<CreditAssessment> findByCreditCheckId(UUID creditCheckId);

    boolean existsByCreditCheckId(UUID creditCheckId);
}
