package com.fourati.service;

import com.fourati.domain.Customer;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.UpdateCustomerRequest;
import com.fourati.dto.response.CustomerResponse;
import com.fourati.mapper.CustomerMapper;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CreateCustomerRequest newRequest() {
        return new CreateCustomerRequest(
                "individual",
                "active",
                "Jane",
                "Doe",
                null,
                LocalDate.of(1990, 1, 1),
                "NID-123",
                "jane.doe@example.com",
                "+15551234567"
        );
    }

    @Test
    void create_savesCustomer() {
        CreateCustomerRequest request = newRequest();
        Customer entity = new Customer();

        when(customerRepository.existsByEmail(request.email())).thenReturn(false);
        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(
                new CustomerResponse(UUID.randomUUID(), "individual", "active", "Jane", "Doe",
                        null, request.dateOfBirth(), "NID-123", request.email(), request.phone(), null, null));

        CustomerResponse response = customerService.create(request);

        assertThat(response.email()).isEqualTo(request.email());
        verify(customerRepository).save(entity);
    }

    @Test
    void create_throwsConflict_whenEmailAlreadyExists() {
        CreateCustomerRequest request = newRequest();
        when(customerRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_setsDeletedAt() {
        UUID id = UUID.randomUUID();
        Customer entity = new Customer();
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        customerService.delete(id);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(customerRepository).save(entity);
    }
}
