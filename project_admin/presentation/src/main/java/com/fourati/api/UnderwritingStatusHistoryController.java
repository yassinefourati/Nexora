package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.UnderwritingStatusHistoryResponse;
import com.fourati.service.UnderwritingStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/underwriting-status-history")
@Tag(name = "Underwriting Status History", description = "Read-only audit trail of underwriting case status transitions.")
public class UnderwritingStatusHistoryController {

    private final UnderwritingStatusHistoryService underwritingStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for an underwriting case")
    public ResponseEntity<ApiResponse<List<UnderwritingStatusHistoryResponse>>> listByCase(
            @RequestParam UUID underwritingCaseId) {
        return ApiResponse.ok(underwritingStatusHistoryService.findByUnderwritingCaseId(underwritingCaseId), "Underwriting status history retrieved");
    }
}
