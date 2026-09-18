package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUnderwritingConditionRequest;
import com.fourati.dto.response.UnderwritingConditionResponse;
import com.fourati.service.UnderwritingConditionService;
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
@RequestMapping(ApiConstants.VERSION + "/underwriting-conditions")
@Tag(name = "Underwriting Conditions", description = "Manage conditions attached to an underwriting decision.")
public class UnderwritingConditionController {

    private final UnderwritingConditionService underwritingConditionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Attach a condition to an underwriting case")
    public ResponseEntity<ApiResponse<UnderwritingConditionResponse>> create(
            @Valid @RequestBody CreateUnderwritingConditionRequest request) {
        UnderwritingConditionResponse created = underwritingConditionService.create(request);
        return ApiResponse.created(created, "Underwriting condition added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List conditions for an underwriting case")
    public ResponseEntity<ApiResponse<List<UnderwritingConditionResponse>>> listByCase(
            @RequestParam UUID underwritingCaseId) {
        return ApiResponse.ok(underwritingConditionService.findByUnderwritingCaseId(underwritingCaseId), "Underwriting conditions retrieved");
    }

    @PostMapping("/{id}/satisfy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark an underwriting condition as satisfied")
    public ResponseEntity<ApiResponse<UnderwritingConditionResponse>> satisfy(@PathVariable UUID id) {
        return ApiResponse.ok(underwritingConditionService.satisfy(id), "Underwriting condition satisfied");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a condition from an underwriting case")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        underwritingConditionService.delete(id);
        return ApiResponse.noContent();
    }
}
