package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCustomerAddressRequest;
import com.fourati.dto.response.CustomerAddressResponse;
import com.fourati.service.CustomerAddressService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/customer-addresses")
@Tag(name = "Customer Addresses", description = "Manage addresses on file for a customer.")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an address to a customer")
    public ResponseEntity<ApiResponse<CustomerAddressResponse>> create(
            @Valid @RequestBody CreateCustomerAddressRequest request) {
        CustomerAddressResponse created = customerAddressService.create(request);
        return ApiResponse.created(created, "Customer address added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List addresses for a customer")
    public ResponseEntity<ApiResponse<List<CustomerAddressResponse>>> listByCustomer(
            @RequestParam UUID customerId) {
        return ApiResponse.ok(customerAddressService.findByCustomerId(customerId), "Customer addresses retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an address from a customer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerAddressService.delete(id);
        return ApiResponse.noContent();
    }
}
