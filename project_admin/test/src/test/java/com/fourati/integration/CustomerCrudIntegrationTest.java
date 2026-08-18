package com.fourati.integration;

import com.fourati.domain.Customer;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.UpdateCustomerRequest;
import com.fourati.dto.response.CustomerResponse;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.repository.CustomerRepository;
import com.fourati.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end regression test against a REAL Postgres (Testcontainers),
 * covering the core Customers CRUD flow through the real
 * service -> repository -> database path (not mocks), including the
 * soft-delete and email-conflict behavior a real client relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
class CustomerCrudIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private CreateCustomerRequest newCustomerRequest(String suffix) {
        return new CreateCustomerRequest(
                "individual",
                "active",
                "Test",
                "Customer",
                null,
                LocalDate.of(1990, 1, 1),
                null,
                "crud-test-" + suffix + "@example.com",
                "+15550000000");
    }

    @Test
    void create_persistsCustomerAndIsRetrievableById() {
        String suffix = UUID.randomUUID().toString();
        CreateCustomerRequest request = newCustomerRequest(suffix);

        CustomerResponse created = customerService.create(request);

        assertThat(created.id()).isNotNull();

        Customer stored = customerRepository.findById(created.id()).orElseThrow();
        assertThat(stored.getEmail()).isEqualTo(request.email());

        customerRepository.deleteById(created.id());
    }

    @Test
    void create_rejectsDuplicateEmail() {
        String suffix = UUID.randomUUID().toString();
        CustomerResponse first = customerService.create(newCustomerRequest(suffix));

        assertThatThrownBy(() -> customerService.create(newCustomerRequest(suffix)))
                .isInstanceOf(ConflictException.class);

        customerRepository.deleteById(first.id());
    }

    @Test
    void update_changesFieldsAndPersists() {
        CustomerResponse created = customerService.create(newCustomerRequest(UUID.randomUUID().toString()));

        UpdateCustomerRequest update = new UpdateCustomerRequest(
                "individual",
                "inactive",
                "Updated",
                "Name",
                null,
                created.dateOfBirth(),
                null,
                "updated-" + created.id() + "@example.com",
                "+15559999999");

        CustomerResponse updated = customerService.update(created.id(), update);

        assertThat(updated.email()).isEqualTo(update.email());
        assertThat(updated.firstName()).isEqualTo("Updated");
        assertThat(updated.status()).isEqualTo("inactive");

        customerRepository.deleteById(created.id());
    }

    @Test
    void delete_makesCustomerUnreadableThroughTheNormalReadPath() {
        CustomerResponse created = customerService.create(newCustomerRequest(UUID.randomUUID().toString()));

        customerService.delete(created.id());

        assertThatThrownBy(() -> customerService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(customerRepository.findById(created.id())).isEmpty();
    }
}
