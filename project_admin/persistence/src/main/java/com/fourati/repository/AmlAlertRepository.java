package com.fourati.repository;

import com.fourati.domain.AmlAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AmlAlertRepository extends JpaRepository<AmlAlert, UUID> {

    List<AmlAlert> findByAmlScreeningId(UUID amlScreeningId);
}
