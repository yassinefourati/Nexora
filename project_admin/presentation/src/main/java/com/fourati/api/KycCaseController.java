package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CompleteKycCaseRequest;
import com.fourati.dto.request.CreateKycCaseRequest;
import com.fourati.dto.response.KycCaseResponse;
import com.fourati.service.KycCaseService;
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
@RequestMapping(ApiConstants.VERSION + "/kyc-cases")
@Tag(name = "KYC Cases", description = "Manage customer identity-verification (Know Your Customer) cases.")
public class KycCaseController {

    private final KycCaseService kycCaseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new KYC case for a customer")
    public ResponseEntity<ApiResponse<KycCaseResponse>> create(@Valid @RequestBody CreateKycCaseRequest request) {
        KycCaseResponse created = kycCaseService.create(request);
        return ApiResponse.created(created, "KYC case opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a KYC case by id")
    public ResponseEntity<ApiResponse<KycCaseResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(kycCaseService.findById(id), "KYC case retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List KYC cases (paginated)")
    public ResponseEntity<ApiResponse<List<KycCaseResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(kycCaseService.findAll(pageable), "KYC cases retrieved");
    }

    @PostMapping("/{id}/start-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Move a pending KYC case into progress")
    public ResponseEntity<ApiResponse<KycCaseResponse>> startReview(@PathVariable UUID id) {
        return ApiResponse.ok(kycCaseService.startReview(id), "KYC case review started");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Complete a KYC case with an outcome")
    public ResponseEntity<ApiResponse<KycCaseResponse>> complete(@PathVariable UUID id,
            @Valid @RequestBody CompleteKycCaseRequest request) {
        return ApiResponse.ok(kycCaseService.complete(id, request), "KYC case completed");
    }
}
