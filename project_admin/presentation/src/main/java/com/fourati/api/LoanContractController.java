package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CancelLoanContractRequest;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.request.FinalizeLoanContractRequest;
import com.fourati.dto.response.LoanContractResponse;
import com.fourati.service.LoanContractService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/loan-contracts")
@Tag(name = "Loan Contracts", description = "Generate and manage the contract document for an accepted loan offer.")
public class LoanContractController {

    private final LoanContractService loanContractService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate a new loan contract")
    public ResponseEntity<ApiResponse<LoanContractResponse>> create(@Valid @RequestBody CreateLoanContractRequest request) {
        LoanContractResponse created = loanContractService.create(request);
        return ApiResponse.created(created, "Loan contract generated successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan contract by id")
    public ResponseEntity<ApiResponse<LoanContractResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanContractService.findById(id), "Loan contract retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan contracts (paginated)")
    public ResponseEntity<ApiResponse<List<LoanContractResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanContractService.findAll(pageable), "Loan contracts retrieved");
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finalize a loan contract")
    public ResponseEntity<ApiResponse<LoanContractResponse>> finalizeContract(@PathVariable UUID id,
            @Valid @RequestBody FinalizeLoanContractRequest request) {
        return ApiResponse.ok(loanContractService.finalizeContract(id, request), "Loan contract finalized");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a loan contract")
    public ResponseEntity<ApiResponse<LoanContractResponse>> cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelLoanContractRequest request) {
        return ApiResponse.ok(loanContractService.cancel(id, request), "Loan contract cancelled");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan contract")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanContractService.delete(id);
        return ApiResponse.noContent();
    }
}
