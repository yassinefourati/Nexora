package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCustomerRequest;
import com.fourati.dto.request.UpdateCustomerRequest;
import com.fourati.dto.response.CustomerResponse;
import com.fourati.service.CustomerService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/customers")
@Tag(name = "Customers", description = "Manage loan platform customers (individual or business applicants).")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ApiResponse.created(created, "Customer created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a customer by id")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(customerService.findById(id), "Customer retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List customers (paginated)")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(customerService.findAll(pageable), "Customers retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.ok(customerService.update(id, request), "Customer updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a customer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ApiResponse.noContent();
    }
}
