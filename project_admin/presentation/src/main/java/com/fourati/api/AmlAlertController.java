package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateAmlAlertRequest;
import com.fourati.dto.response.AmlAlertResponse;
import com.fourati.service.AmlAlertService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/aml-alerts")
@Tag(name = "AML Alerts", description = "Manage alerts raised from AML screening matches.")
public class AmlAlertController {

    private final AmlAlertService amlAlertService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Raise an AML alert")
    public ResponseEntity<ApiResponse<AmlAlertResponse>> create(@Valid @RequestBody CreateAmlAlertRequest request) {
        AmlAlertResponse created = amlAlertService.create(request);
        return ApiResponse.created(created, "AML alert raised successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List AML alerts for a screening")
    public ResponseEntity<ApiResponse<List<AmlAlertResponse>>> listByAmlScreening(@RequestParam UUID amlScreeningId) {
        return ApiResponse.ok(amlAlertService.findByAmlScreeningId(amlScreeningId), "AML alerts retrieved");
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve an AML alert")
    public ResponseEntity<ApiResponse<AmlAlertResponse>> resolve(@PathVariable UUID id) {
        return ApiResponse.ok(amlAlertService.resolve(id), "AML alert resolved");
    }
}
