package com.fourati.repository;

import com.fourati.domain.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditScoreRepository extends JpaRepository<CreditScore, UUID> {

    List<CreditScore> findByCreditCheckId(UUID creditCheckId);
}
