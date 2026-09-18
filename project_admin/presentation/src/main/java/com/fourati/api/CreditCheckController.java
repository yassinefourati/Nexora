package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCreditCheckRequest;
import com.fourati.dto.response.CreditCheckResponse;
import com.fourati.service.CreditCheckService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/credit-checks")
@Tag(name = "Credit Checks", description = "Manage credit bureau checks performed for a loan application.")
public class CreditCheckController {

    private final CreditCheckService creditCheckService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Request a credit check for a loan application")
    public ResponseEntity<ApiResponse<CreditCheckResponse>> create(@Valid @RequestBody CreateCreditCheckRequest request) {
        CreditCheckResponse created = creditCheckService.create(request);
        return ApiResponse.created(created, "Credit check requested successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a credit check by id")
    public ResponseEntity<ApiResponse<CreditCheckResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(creditCheckService.findById(id), "Credit check retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List credit checks (paginated)")
    public ResponseEntity<ApiResponse<List<CreditCheckResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(creditCheckService.findAll(pageable), "Credit checks retrieved");
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve the credit report and assess a pending credit check")
    public ResponseEntity<ApiResponse<CreditCheckResponse>> process(@PathVariable UUID id) {
        return ApiResponse.ok(creditCheckService.process(id), "Credit check processed");
    }
}
