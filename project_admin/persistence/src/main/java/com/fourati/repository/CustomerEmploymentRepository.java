package com.fourati.repository;

import com.fourati.domain.CustomerEmployment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerEmploymentRepository extends JpaRepository<CustomerEmployment, UUID> {

    List<CustomerEmployment> findByCustomerId(UUID customerId);
}
