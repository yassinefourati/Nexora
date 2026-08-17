package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.KycStatusHistoryResponse;
import com.fourati.service.KycStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/kyc-status-history")
@Tag(name = "KYC Status History", description = "Read-only audit trail of KYC case status transitions.")
public class KycStatusHistoryController {

    private final KycStatusHistoryService kycStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a KYC case")
    public ResponseEntity<ApiResponse<List<KycStatusHistoryResponse>>> listByKycCase(@RequestParam UUID kycCaseId) {
        return ApiResponse.ok(kycStatusHistoryService.findByKycCaseId(kycCaseId), "KYC status history retrieved");
    }
}
