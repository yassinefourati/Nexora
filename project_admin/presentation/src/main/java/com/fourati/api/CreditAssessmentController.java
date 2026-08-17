package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.CreditAssessmentResponse;
import com.fourati.service.CreditAssessmentService;
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
@RequestMapping(ApiConstants.VERSION + "/credit-assessments")
@Tag(name = "Credit Assessments", description = "Read-only credit decisions derived from a credit check.")
public class CreditAssessmentController {

    private final CreditAssessmentService creditAssessmentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List credit assessments for a credit check")
    public ResponseEntity<ApiResponse<List<CreditAssessmentResponse>>> listByCreditCheck(@RequestParam UUID creditCheckId) {
        return ApiResponse.ok(creditAssessmentService.findByCreditCheckId(creditCheckId), "Credit assessments retrieved");
    }
}
