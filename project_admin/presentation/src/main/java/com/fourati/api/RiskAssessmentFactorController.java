package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.RiskAssessmentFactorResponse;
import com.fourati.service.RiskAssessmentFactorService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/risk-assessment-factors")
@Tag(name = "Risk Assessment Factors", description = "Read-only weighted input factors for a risk assessment.")
public class RiskAssessmentFactorController {

    private final RiskAssessmentFactorService riskAssessmentFactorService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List factors for a risk assessment")
    public ResponseEntity<ApiResponse<List<RiskAssessmentFactorResponse>>> listByRiskAssessment(
            @RequestParam UUID riskAssessmentId) {
        return ApiResponse.ok(riskAssessmentFactorService.findByRiskAssessmentId(riskAssessmentId), "Risk assessment factors retrieved");
    }
}
