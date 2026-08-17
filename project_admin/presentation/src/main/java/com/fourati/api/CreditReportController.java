package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.CreditReportResponse;
import com.fourati.service.CreditReportService;
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
@RequestMapping(ApiConstants.VERSION + "/credit-reports")
@Tag(name = "Credit Reports", description = "Read-only credit bureau reports retrieved for a credit check.")
public class CreditReportController {

    private final CreditReportService creditReportService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List credit reports for a credit check")
    public ResponseEntity<ApiResponse<List<CreditReportResponse>>> listByCreditCheck(@RequestParam UUID creditCheckId) {
        return ApiResponse.ok(creditReportService.findByCreditCheckId(creditCheckId), "Credit reports retrieved");
    }
}
