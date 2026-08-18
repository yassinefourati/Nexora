package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.request.DecideUnderwritingCaseRequest;
import com.fourati.dto.response.UnderwritingCaseResponse;
import com.fourati.service.UnderwritingCaseService;
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
@RequestMapping(ApiConstants.VERSION + "/underwriting-cases")
@Tag(name = "Underwriting Cases", description = "Manage underwriting decisions on loan applications.")
public class UnderwritingCaseController {

    private final UnderwritingCaseService underwritingCaseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new underwriting case for a loan application")
    public ResponseEntity<ApiResponse<UnderwritingCaseResponse>> create(@Valid @RequestBody CreateUnderwritingCaseRequest request) {
        UnderwritingCaseResponse created = underwritingCaseService.create(request);
        return ApiResponse.created(created, "Underwriting case opened successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get an underwriting case by id")
    public ResponseEntity<ApiResponse<UnderwritingCaseResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(underwritingCaseService.findById(id), "Underwriting case retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List underwriting cases (paginated)")
    public ResponseEntity<ApiResponse<List<UnderwritingCaseResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(underwritingCaseService.findAll(pageable), "Underwriting cases retrieved");
    }

    @PostMapping("/{id}/start-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Start reviewing an underwriting case")
    public ResponseEntity<ApiResponse<UnderwritingCaseResponse>> startReview(@PathVariable UUID id) {
        return ApiResponse.ok(underwritingCaseService.startReview(id), "Underwriting case review started");
    }

    @PostMapping("/{id}/decide")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record the underwriting decision for a case")
    public ResponseEntity<ApiResponse<UnderwritingCaseResponse>> decide(@PathVariable UUID id,
            @Valid @RequestBody DecideUnderwritingCaseRequest request) {
        return ApiResponse.ok(underwritingCaseService.decide(id, request), "Underwriting decision recorded");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete an underwriting case")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        underwritingCaseService.delete(id);
        return ApiResponse.noContent();
    }
}
