package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.request.DeclineLoanOfferRequest;
import com.fourati.dto.response.LoanOfferResponse;
import com.fourati.service.LoanOfferService;
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
@RequestMapping(ApiConstants.VERSION + "/loan-offers")
@Tag(name = "Loan Offers", description = "Present approved loan terms to the customer for acceptance or decline.")
public class LoanOfferController {

    private final LoanOfferService loanOfferService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Issue a new loan offer")
    public ResponseEntity<ApiResponse<LoanOfferResponse>> create(@Valid @RequestBody CreateLoanOfferRequest request) {
        LoanOfferResponse created = loanOfferService.create(request);
        return ApiResponse.created(created, "Loan offer issued successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a loan offer by id")
    public ResponseEntity<ApiResponse<LoanOfferResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loanOfferService.findById(id), "Loan offer retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan offers (paginated)")
    public ResponseEntity<ApiResponse<List<LoanOfferResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(loanOfferService.findAll(pageable), "Loan offers retrieved");
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Accept a loan offer")
    public ResponseEntity<ApiResponse<LoanOfferResponse>> accept(@PathVariable UUID id) {
        return ApiResponse.ok(loanOfferService.accept(id), "Loan offer accepted");
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Decline a loan offer")
    public ResponseEntity<ApiResponse<LoanOfferResponse>> decline(@PathVariable UUID id,
            @Valid @RequestBody DeclineLoanOfferRequest request) {
        return ApiResponse.ok(loanOfferService.decline(id, request), "Loan offer declined");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a loan offer")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanOfferService.delete(id);
        return ApiResponse.noContent();
    }
}
