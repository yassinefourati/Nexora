package com.fourati.repository;

import com.fourati.domain.CustomerConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerConsentRepository extends JpaRepository<CustomerConsent, UUID> {

    List<CustomerConsent> findByCustomerId(UUID customerId);
}
