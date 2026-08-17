package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.FraudAlertResponse;
import com.fourati.service.FraudAlertService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/fraud-alerts")
@Tag(name = "Fraud Alerts", description = "Manage alerts raised from a fraud check.")
public class FraudAlertController {

    private final FraudAlertService fraudAlertService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List fraud alerts for a fraud check")
    public ResponseEntity<ApiResponse<List<FraudAlertResponse>>> listByFraudCheck(@RequestParam UUID fraudCheckId) {
        return ApiResponse.ok(fraudAlertService.findByFraudCheckId(fraudCheckId), "Fraud alerts retrieved");
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve a fraud alert")
    public ResponseEntity<ApiResponse<FraudAlertResponse>> resolve(@PathVariable UUID id) {
        return ApiResponse.ok(fraudAlertService.resolve(id), "Fraud alert resolved");
    }
}
