package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CancelLoanApplicationRequest;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.UpdateLoanApplicationRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import com.fourati.service.LoanApplicationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/loan-applications")
@Tag(name = "Loan Applications", description = "Manage customer applications for a loan product.")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new loan application (draft)")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> create(@Valid @RequestBody CreateLoanApplicationRequest request) {
        LoanApplicationResponse created = loanApplicationService.create(request);
        return ApiResponse.created(created, "Loan application created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan application by id")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanApplicationService.findById(id), "Loan application retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan applications (paginated)")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanApplicationService.findAll(pageable), "Loan applications retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a draft loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateLoanApplicationRequest request) {
        return ApiResponse.ok(loanApplicationService.update(id, request), "Loan application updated successfully");
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit a draft loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> submit(@PathVariable UUID id) {
        return ApiResponse.ok(loanApplicationService.submit(id), "Loan application submitted");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel a loan application")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> cancel(@PathVariable UUID id,
            @Valid @RequestBody CancelLoanApplicationRequest request) {
        return ApiResponse.ok(loanApplicationService.cancel(id, request), "Loan application cancelled");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan application")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApplicationService.delete(id);
        return ApiResponse.noContent();
    }
}
