package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.domain.CustomerAddress;
import com.fourati.dto.request.CreateCustomerAddressRequest;
import com.fourati.dto.response.CustomerAddressResponse;
import com.fourati.mapper.CustomerAddressMapper;
import com.fourati.repository.CustomerAddressRepository;
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
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;
    private final CustomerAddressMapper customerAddressMapper;

    @Audited(action = "CREATE", description = "Add an address to a customer")
    public CustomerAddressResponse create(CreateCustomerAddressRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        CustomerAddress entity = customerAddressMapper.toEntity(request);
        entity.setCustomer(customer);
        CustomerAddress saved = customerAddressRepository.save(entity);
        return customerAddressMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> findByCustomerId(UUID customerId) {
        return customerAddressRepository.findByCustomerId(customerId).stream()
                .map(customerAddressMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove an address from a customer")
    public void delete(UUID id) {
        CustomerAddress entity = customerAddressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", id));
        customerAddressRepository.delete(entity);
    }
}
