package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateKycCheckRequest;
import com.fourati.dto.response.KycCheckResponse;
import com.fourati.service.KycCheckService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/kyc-checks")
@Tag(name = "KYC Checks", description = "Manage individual verification checks within a KYC case.")
public class KycCheckController {

    private final KycCheckService kycCheckService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a KYC check result")
    public ResponseEntity<ApiResponse<KycCheckResponse>> create(@Valid @RequestBody CreateKycCheckRequest request) {
        KycCheckResponse created = kycCheckService.create(request);
        return ApiResponse.created(created, "KYC check recorded successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List checks for a KYC case")
    public ResponseEntity<ApiResponse<List<KycCheckResponse>>> listByKycCase(@RequestParam UUID kycCaseId) {
        return ApiResponse.ok(kycCheckService.findByKycCaseId(kycCaseId), "KYC checks retrieved");
    }
}
