package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCustomerConsentRequest;
import com.fourati.dto.response.CustomerConsentResponse;
import com.fourati.service.CustomerConsentService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/customer-consents")
@Tag(name = "Customer Consents", description = "Manage consent decisions on file for a customer.")
public class CustomerConsentController {

    private final CustomerConsentService customerConsentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a consent decision for a customer")
    public ResponseEntity<ApiResponse<CustomerConsentResponse>> create(
            @Valid @RequestBody CreateCustomerConsentRequest request) {
        CustomerConsentResponse created = customerConsentService.create(request);
        return ApiResponse.created(created, "Customer consent recorded successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List consent decisions for a customer")
    public ResponseEntity<ApiResponse<List<CustomerConsentResponse>>> listByCustomer(
            @RequestParam UUID customerId) {
        return ApiResponse.ok(customerConsentService.findByCustomerId(customerId), "Customer consents retrieved");
    }

    @PutMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke a customer consent")
    public ResponseEntity<ApiResponse<CustomerConsentResponse>> revoke(@PathVariable UUID id) {
        return ApiResponse.ok(customerConsentService.revoke(id), "Customer consent revoked");
    }
}
