package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.CreditCheckStatusHistoryResponse;
import com.fourati.service.CreditCheckStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/credit-check-status-history")
@Tag(name = "Credit Check Status History", description = "Read-only audit trail of credit check status transitions.")
public class CreditCheckStatusHistoryController {

    private final CreditCheckStatusHistoryService creditCheckStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a credit check")
    public ResponseEntity<ApiResponse<List<CreditCheckStatusHistoryResponse>>> listByCreditCheck(@RequestParam UUID creditCheckId) {
        return ApiResponse.ok(creditCheckStatusHistoryService.findByCreditCheckId(creditCheckId), "Credit check status history retrieved");
    }
}
