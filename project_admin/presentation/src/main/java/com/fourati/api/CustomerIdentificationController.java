package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCustomerIdentificationRequest;
import com.fourati.dto.response.CustomerIdentificationResponse;
import com.fourati.service.CustomerIdentificationService;
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
@RequestMapping(ApiConstants.VERSION + "/customer-identifications")
@Tag(name = "Customer Identifications", description = "Manage identity documents on file for a customer.")
public class CustomerIdentificationController {

    private final CustomerIdentificationService customerIdentificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an identification document to a customer")
    public ResponseEntity<ApiResponse<CustomerIdentificationResponse>> create(
            @Valid @RequestBody CreateCustomerIdentificationRequest request) {
        CustomerIdentificationResponse created = customerIdentificationService.create(request);
        return ApiResponse.created(created, "Customer identification document added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List identification documents for a customer")
    public ResponseEntity<ApiResponse<List<CustomerIdentificationResponse>>> listByCustomer(
            @RequestParam UUID customerId) {
        return ApiResponse.ok(customerIdentificationService.findByCustomerId(customerId), "Customer identification documents retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an identification document from a customer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerIdentificationService.delete(id);
        return ApiResponse.noContent();
    }
}
