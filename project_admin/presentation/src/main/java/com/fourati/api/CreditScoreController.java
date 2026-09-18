package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.CreditScoreResponse;
import com.fourati.service.CreditScoreService;
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
@RequestMapping(ApiConstants.VERSION + "/credit-scores")
@Tag(name = "Credit Scores", description = "Read-only normalized credit scores computed for a credit check.")
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List credit scores for a credit check")
    public ResponseEntity<ApiResponse<List<CreditScoreResponse>>> listByCreditCheck(@RequestParam UUID creditCheckId) {
        return ApiResponse.ok(creditScoreService.findByCreditCheckId(creditCheckId), "Credit scores retrieved");
    }
}
