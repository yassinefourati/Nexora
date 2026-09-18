package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateAmlScreeningRequest;
import com.fourati.dto.response.AmlScreeningResponse;
import com.fourati.service.AmlScreeningService;
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
@RequestMapping(ApiConstants.VERSION + "/aml-screenings")
@Tag(name = "AML Screenings", description = "Manage Anti-Money-Laundering screening results for a KYC case.")
public class AmlScreeningController {

    private final AmlScreeningService amlScreeningService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record an AML screening result")
    public ResponseEntity<ApiResponse<AmlScreeningResponse>> create(@Valid @RequestBody CreateAmlScreeningRequest request) {
        AmlScreeningResponse created = amlScreeningService.create(request);
        return ApiResponse.created(created, "AML screening recorded successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List AML screenings for a KYC case")
    public ResponseEntity<ApiResponse<List<AmlScreeningResponse>>> listByKycCase(@RequestParam UUID kycCaseId) {
        return ApiResponse.ok(amlScreeningService.findByKycCaseId(kycCaseId), "AML screenings retrieved");
    }
}
