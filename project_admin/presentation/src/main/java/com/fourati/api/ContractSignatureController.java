package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.request.DeclineContractSignatureRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import com.fourati.service.ContractSignatureService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/contract-signatures")
@Tag(name = "Contract Signatures", description = "Capture signer signatures on a finalized loan contract.")
public class ContractSignatureController {

    private final ContractSignatureService contractSignatureService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Request a signature on a loan contract")
    public ResponseEntity<ApiResponse<ContractSignatureResponse>> create(@Valid @RequestBody CreateContractSignatureRequest request) {
        ContractSignatureResponse created = contractSignatureService.create(request);
        return ApiResponse.created(created, "Contract signature requested successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a contract signature by id")
    public ResponseEntity<ApiResponse<ContractSignatureResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(contractSignatureService.findById(id), "Contract signature retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List contract signatures, optionally filtered by loan contract (paginated)")
    public ResponseEntity<ApiResponse<List<ContractSignatureResponse>>> list(
            @RequestParam(required = false) UUID loanContractId, Pageable pageable) {
        if (loanContractId != null) {
            return ApiResponse.ok(contractSignatureService.findByLoanContractId(loanContractId), "Contract signatures retrieved");
        }
        return ApiResponse.paged(contractSignatureService.findAll(pageable), "Contract signatures retrieved");
    }

    @PostMapping("/{id}/sign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a signature as signed")
    public ResponseEntity<ApiResponse<ContractSignatureResponse>> sign(@PathVariable UUID id) {
        return ApiResponse.ok(contractSignatureService.sign(id), "Contract signature recorded");
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Decline a signature request")
    public ResponseEntity<ApiResponse<ContractSignatureResponse>> decline(@PathVariable UUID id,
            @Valid @RequestBody DeclineContractSignatureRequest request) {
        return ApiResponse.ok(contractSignatureService.decline(id, request), "Contract signature declined");
    }
}
