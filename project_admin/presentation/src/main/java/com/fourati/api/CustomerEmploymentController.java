package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCustomerEmploymentRequest;
import com.fourati.dto.response.CustomerEmploymentResponse;
import com.fourati.service.CustomerEmploymentService;
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
@RequestMapping(ApiConstants.VERSION + "/customer-employments")
@Tag(name = "Customer Employments", description = "Manage employment records on file for a customer.")
public class CustomerEmploymentController {

    private final CustomerEmploymentService customerEmploymentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add an employment record to a customer")
    public ResponseEntity<ApiResponse<CustomerEmploymentResponse>> create(
            @Valid @RequestBody CreateCustomerEmploymentRequest request) {
        CustomerEmploymentResponse created = customerEmploymentService.create(request);
        return ApiResponse.created(created, "Customer employment record added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List employment records for a customer")
    public ResponseEntity<ApiResponse<List<CustomerEmploymentResponse>>> listByCustomer(
            @RequestParam UUID customerId) {
        return ApiResponse.ok(customerEmploymentService.findByCustomerId(customerId), "Customer employment records retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove an employment record from a customer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerEmploymentService.delete(id);
        return ApiResponse.noContent();
    }
}
