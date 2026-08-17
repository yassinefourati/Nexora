package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CustomerIdentification;
import com.fourati.dto.request.CreateCustomerIdentificationRequest;
import com.fourati.dto.response.CustomerIdentificationResponse;
import com.fourati.mapper.CustomerIdentificationMapper;
import com.fourati.repository.CustomerIdentificationRepository;
import com.fourati.repository.CustomerRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerIdentificationService {

    private final CustomerIdentificationRepository customerIdentificationRepository;
    private final CustomerRepository customerRepository;
    private final CustomerIdentificationMapper customerIdentificationMapper;

    @Audited(action = "CREATE", description = "Add an identification document to a customer")
    public CustomerIdentificationResponse create(CreateCustomerIdentificationRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        CustomerIdentification entity = customerIdentificationMapper.toEntity(request);
        entity.setCustomer(customer);
        CustomerIdentification saved = customerIdentificationRepository.save(entity);
        return customerIdentificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerIdentificationResponse> findByCustomerId(UUID customerId) {
        return customerIdentificationRepository.findByCustomerId(customerId).stream()
                .map(customerIdentificationMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an identification document from a customer")
    public void delete(UUID id) {
        CustomerIdentification entity = customerIdentificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerIdentification", id));
        customerIdentificationRepository.delete(entity);
    }
}
