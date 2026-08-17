package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.RiskAssessmentStatusHistoryResponse;
import com.fourati.service.RiskAssessmentStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/risk-assessment-status-history")
@Tag(name = "Risk Assessment Status History", description = "Read-only audit trail of risk assessment status transitions.")
public class RiskAssessmentStatusHistoryController {

    private final RiskAssessmentStatusHistoryService riskAssessmentStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a risk assessment")
    public ResponseEntity<ApiResponse<List<RiskAssessmentStatusHistoryResponse>>> listByRiskAssessment(
            @RequestParam UUID riskAssessmentId) {
        return ApiResponse.ok(riskAssessmentStatusHistoryService.findByRiskAssessmentId(riskAssessmentId), "Risk assessment status history retrieved");
    }
}
