package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CustomerEmployment;
import com.fourati.dto.request.CreateCustomerEmploymentRequest;
import com.fourati.dto.response.CustomerEmploymentResponse;
import com.fourati.mapper.CustomerEmploymentMapper;
import com.fourati.repository.CustomerEmploymentRepository;
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
public class CustomerEmploymentService {

    private final CustomerEmploymentRepository customerEmploymentRepository;
    private final CustomerRepository customerRepository;
    private final CustomerEmploymentMapper customerEmploymentMapper;

    @Audited(action = "CREATE", description = "Add an employment record to a customer")
    public CustomerEmploymentResponse create(CreateCustomerEmploymentRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        CustomerEmployment entity = customerEmploymentMapper.toEntity(request);
        entity.setCustomer(customer);
        CustomerEmployment saved = customerEmploymentRepository.save(entity);
        return customerEmploymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerEmploymentResponse> findByCustomerId(UUID customerId) {
        return customerEmploymentRepository.findByCustomerId(customerId).stream()
                .map(customerEmploymentMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an employment record from a customer")
    public void delete(UUID id) {
        CustomerEmployment entity = customerEmploymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerEmployment", id));
        customerEmploymentRepository.delete(entity);
    }
}
