package com.fourati.repository;

import com.fourati.domain.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {

    List<FraudAlert> findByFraudCheckId(UUID fraudCheckId);
}
