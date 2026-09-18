package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanApplicationCoApplicantRequest;
import com.fourati.dto.response.LoanApplicationCoApplicantResponse;
import com.fourati.service.LoanApplicationCoApplicantService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping(ApiConstants.VERSION + "/loan-application-co-applicants")
@Tag(name = "Loan Application Co-Applicants", description = "Manage co-applicants on a loan application.")
public class LoanApplicationCoApplicantController {

    private final LoanApplicationCoApplicantService loanApplicationCoApplicantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a co-applicant to a loan application")
    public ResponseEntity<ApiResponse<LoanApplicationCoApplicantResponse>> create(
            @Valid @RequestBody CreateLoanApplicationCoApplicantRequest request) {
        LoanApplicationCoApplicantResponse created = loanApplicationCoApplicantService.create(request);
        return ApiResponse.created(created, "Loan application co-applicant added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List co-applicants for a loan application")
    public ResponseEntity<ApiResponse<List<LoanApplicationCoApplicantResponse>>> listByLoanApplication(
            @RequestParam UUID loanApplicationId) {
        return ApiResponse.ok(loanApplicationCoApplicantService.findByLoanApplicationId(loanApplicationId), "Loan application co-applicants retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a co-applicant from a loan application")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        loanApplicationCoApplicantService.delete(id);
        return ApiResponse.noContent();
    }
}
