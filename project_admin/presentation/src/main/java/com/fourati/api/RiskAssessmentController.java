package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateRiskAssessmentRequest;
import com.fourati.dto.response.RiskAssessmentResponse;
import com.fourati.service.RiskAssessmentService;
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
@RequestMapping(ApiConstants.VERSION + "/risk-assessments")
@Tag(name = "Risk Assessments", description = "Manage overall risk assessments performed for a loan application.")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a risk assessment for a loan application")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> create(@Valid @RequestBody CreateRiskAssessmentRequest request) {
        RiskAssessmentResponse created = riskAssessmentService.create(request);
        return ApiResponse.created(created, "Risk assessment opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a risk assessment by id")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(riskAssessmentService.findById(id), "Risk assessment retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List risk assessments (paginated)")
    public ResponseEntity<ApiResponse<List<RiskAssessmentResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(riskAssessmentService.findAll(pageable), "Risk assessments retrieved");
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Score and classify a pending risk assessment")
    public ResponseEntity<ApiResponse<RiskAssessmentResponse>> process(@PathVariable UUID id) {
        return ApiResponse.ok(riskAssessmentService.process(id), "Risk assessment processed");
    }
}
