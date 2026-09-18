package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.UpdateCustomerRequest;
import com.fourati.dto.response.CustomerResponse;
import com.fourati.mapper.CustomerMapper;
import com.fourati.repository.CustomerRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Audited(action = "CREATE", description = "Create a new customer")
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Customer already exists with email: " + request.email());
        }
        Customer entity = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(entity);
        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a customer")
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        Customer entity = getEntityOrThrow(id);
        if (!entity.getEmail().equals(request.email()) && customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Customer already exists with email: " + request.email());
        }
        customerMapper.updateEntityFromRequest(request, entity);
        Customer saved = customerRepository.save(entity);
        return customerMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a customer")
    public void delete(UUID id) {
        Customer entity = getEntityOrThrow(id);
        entity.setDeletedAt(Instant.now());
        customerRepository.save(entity);
    }

    private Customer getEntityOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
