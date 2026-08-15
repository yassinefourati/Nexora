package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateLoginHistoryRequest;
import com.fourati.dto.request.UpdateLoginHistoryRequest;
import com.fourati.dto.response.LoginHistoryResponse;
import com.fourati.service.LoginHistoryService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * API for a user's login session history. Records are created on login and
 * closed (logout) via a targeted update; there is no delete endpoint.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/login-history")
@Tag(name = "Login History", description = "Track user login/logout session history")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Record a new login history entry")
    public ResponseEntity<ApiResponse<LoginHistoryResponse>> create(
            @Valid @RequestBody CreateLoginHistoryRequest request) {
        LoginHistoryResponse created = loginHistoryService.create(request);
        return ApiResponse.created(created, "Login history entry recorded");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a single login history entry by id")
    public ResponseEntity<ApiResponse<LoginHistoryResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loginHistoryService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List login history entries, optionally filtered by user")
    public ResponseEntity<ApiResponse<java.util.List<LoginHistoryResponse>>> list(
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        Page<LoginHistoryResponse> page = userId != null
                ? loginHistoryService.findByUserId(userId, pageable)
                : loginHistoryService.findAll(pageable);
        return ApiResponse.paged(page);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close a login history session by recording its logout timestamp")
    public ResponseEntity<ApiResponse<LoginHistoryResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateLoginHistoryRequest request) {
        return ApiResponse.ok(loginHistoryService.update(id, request), "Login history entry updated");
    }
}
