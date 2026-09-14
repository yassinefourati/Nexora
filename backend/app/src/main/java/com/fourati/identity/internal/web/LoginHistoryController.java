package com.fourati.identity.internal.web;

import com.fourati.identity.LoginHistoryResponse;
import com.fourati.identity.internal.application.LoginHistoryService;
import com.fourati.platform.web.ApiResponse;
import com.fourati.shared.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Read-only view of users' login sessions. The rows are written by the
 * identity provider integration, never by API clients (Phase 4: log tables are
 * read-only from the outside).
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/login-history")
@Tag(name = "Login History", description = "Inspect user login/logout session history")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get a single login history entry by id")
    public ResponseEntity<ApiResponse<LoginHistoryResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(loginHistoryService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List login history entries, optionally filtered by user")
    public ResponseEntity<ApiResponse<List<LoginHistoryResponse>>> list(
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        Page<LoginHistoryResponse> page = userId != null
                ? loginHistoryService.findByUserId(userId, pageable)
                : loginHistoryService.findAll(pageable);
        return ApiResponse.paged(page);
    }
}
