package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateFraudCheckRequest;
import com.fourati.dto.response.FraudCheckResponse;
import com.fourati.service.FraudCheckService;
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
@RequestMapping(ApiConstants.VERSION + "/fraud-checks")
@Tag(name = "Fraud Checks", description = "Manage fraud screening performed for a loan application.")
public class FraudCheckController {

    private final FraudCheckService fraudCheckService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a fraud check for a loan application")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> create(@Valid @RequestBody CreateFraudCheckRequest request) {
        FraudCheckResponse created = fraudCheckService.create(request);
        return ApiResponse.created(created, "Fraud check opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a fraud check by id")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(fraudCheckService.findById(id), "Fraud check retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List fraud checks (paginated)")
    public ResponseEntity<ApiResponse<List<FraudCheckResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(fraudCheckService.findAll(pageable), "Fraud checks retrieved");
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Score a pending fraud check and raise an alert if flagged")
    public ResponseEntity<ApiResponse<FraudCheckResponse>> process(@PathVariable UUID id) {
        return ApiResponse.ok(fraudCheckService.process(id), "Fraud check processed");
    }
}
