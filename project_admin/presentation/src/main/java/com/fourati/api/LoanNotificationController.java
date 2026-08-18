package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoanNotificationRequest;
import com.fourati.dto.response.LoanNotificationResponse;
import com.fourati.service.LoanNotificationService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/loan-notifications")
@Tag(name = "Loan Notifications", description = "Notifications sent about a loan application's lifecycle events.")
public class LoanNotificationController {

    private final LoanNotificationService loanNotificationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send a notification about a loan application event")
    public ResponseEntity<ApiResponse<LoanNotificationResponse>> create(
            @Valid @RequestBody CreateLoanNotificationRequest request) {
        LoanNotificationResponse created = loanNotificationService.create(request);
        return ApiResponse.created(created, "Loan notification sent successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List loan notifications, optionally filtered by loan application (paginated)")
    public ResponseEntity<ApiResponse<List<LoanNotificationResponse>>> list(
            @RequestParam(required = false) UUID loanApplicationId, Pageable pageable) {
        if (loanApplicationId != null) {
            return ApiResponse.ok(loanNotificationService.findByLoanApplicationId(loanApplicationId), "Loan notifications retrieved");
        }
        return ApiResponse.paged(loanNotificationService.findAll(pageable), "Loan notifications retrieved");
    }
}
