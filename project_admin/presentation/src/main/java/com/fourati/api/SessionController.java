package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateSessionRequest;
import com.fourati.dto.request.UpdateSessionRequest;
import com.fourati.dto.response.SessionResponse;
import com.fourati.service.SessionService;
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

import java.util.List;
import java.util.UUID;

/**
 * API for authentication sessions issued to users. Sessions are created on
 * login and revoked rather than deleted.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/sessions")
@Tag(name = "Sessions", description = "Manage authentication sessions issued to users")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create (issue) a new session")
    public ResponseEntity<ApiResponse<SessionResponse>> create(@Valid @RequestBody CreateSessionRequest request) {
        SessionResponse created = sessionService.create(request);
        return ApiResponse.created(created, "Session created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Get a session by id")
    public ResponseEntity<ApiResponse<SessionResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(sessionService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List sessions, optionally filtered by user")
    public ResponseEntity<ApiResponse<List<SessionResponse>>> list(
            @RequestParam(required = false) UUID userId,
            Pageable pageable) {
        Page<SessionResponse> page = userId != null
                ? sessionService.findByUserId(userId, pageable)
                : sessionService.findAll(pageable);
        return ApiResponse.paged(page);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Update a session (e.g. revoke it)")
    public ResponseEntity<ApiResponse<SessionResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateSessionRequest request) {
        return ApiResponse.ok(sessionService.update(id, request), "Session updated successfully");
    }

    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN') or @sessionSecurity.isOwner(#id, authentication)")
    @Operation(summary = "Revoke a session immediately")
    public ResponseEntity<ApiResponse<SessionResponse>> revoke(@PathVariable UUID id) {
        return ApiResponse.ok(sessionService.revoke(id), "Session revoked successfully");
    }
}
