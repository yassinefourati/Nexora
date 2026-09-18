package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CustomerConsent;
import com.fourati.dto.request.CreateCustomerConsentRequest;
import com.fourati.dto.response.CustomerConsentResponse;
import com.fourati.mapper.CustomerConsentMapper;
import com.fourati.repository.CustomerConsentRepository;
import com.fourati.repository.CustomerRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerConsentService {

    private final CustomerConsentRepository customerConsentRepository;
    private final CustomerRepository customerRepository;
    private final CustomerConsentMapper customerConsentMapper;

    @Audited(action = "CREATE", description = "Record a consent decision for a customer")
    public CustomerConsentResponse create(CreateCustomerConsentRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        CustomerConsent entity = customerConsentMapper.toEntity(request);
        entity.setCustomer(customer);
        entity.setGrantedAt(request.granted() ? Instant.now() : null);
        CustomerConsent saved = customerConsentRepository.save(entity);
        return customerConsentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerConsentResponse> findByCustomerId(UUID customerId) {
        return customerConsentRepository.findByCustomerId(customerId).stream()
                .map(customerConsentMapper::toResponse)
                .toList();
    }

    @Audited(action = "REVOKE", description = "Revoke a customer consent")
    public CustomerConsentResponse revoke(UUID id) {
        CustomerConsent entity = customerConsentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerConsent", id));
        entity.setGranted(false);
        entity.setRevokedAt(Instant.now());
        CustomerConsent saved = customerConsentRepository.save(entity);
        return customerConsentMapper.toResponse(saved);
    }
}
