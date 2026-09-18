package com.fourati.repository;

import com.fourati.domain.CustomerIdentification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerIdentificationRepository extends JpaRepository<CustomerIdentification, UUID> {

    List<CustomerIdentification> findByCustomerId(UUID customerId);
}
