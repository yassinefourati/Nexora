package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.ContractSignatureStatusHistoryResponse;
import com.fourati.service.ContractSignatureStatusHistoryService;
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
@RequestMapping(ApiConstants.VERSION + "/contract-signature-status-history")
@Tag(name = "Contract Signature Status History", description = "Read-only audit trail of contract signature status transitions.")
public class ContractSignatureStatusHistoryController {

    private final ContractSignatureStatusHistoryService contractSignatureStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a contract signature")
    public ResponseEntity<ApiResponse<List<ContractSignatureStatusHistoryResponse>>> listBySignature(
            @RequestParam UUID contractSignatureId) {
        return ApiResponse.ok(contractSignatureStatusHistoryService.findByContractSignatureId(contractSignatureId), "Contract signature status history retrieved");
    }
}
