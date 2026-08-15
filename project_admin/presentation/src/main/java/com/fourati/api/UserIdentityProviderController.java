package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUserIdentityProviderRequest;
import com.fourati.dto.response.UserIdentityProviderResponse;
import com.fourati.service.UserIdentityProviderService;
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

/**
 * API for linking/unlinking users to external identity providers (SSO /
 * OAuth2 accounts). There is no update endpoint — unlink and re-link instead.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/user-identity-providers")
@Tag(name = "User Identity Providers", description = "Link and unlink users to external identity providers")
public class UserIdentityProviderController {

    private final UserIdentityProviderService userIdentityProviderService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Link a user to an external identity provider account")
    public ResponseEntity<ApiResponse<UserIdentityProviderResponse>> create(
            @Valid @RequestBody CreateUserIdentityProviderRequest request) {
        UserIdentityProviderResponse created = userIdentityProviderService.create(request);
        return ApiResponse.created(created, "Identity provider linked successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get an identity provider link by id")
    public ResponseEntity<ApiResponse<UserIdentityProviderResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userIdentityProviderService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List identity provider links for a given user")
    public ResponseEntity<ApiResponse<List<UserIdentityProviderResponse>>> list(
            @RequestParam UUID userId) {
        return ApiResponse.ok(userIdentityProviderService.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unlink a user's external identity provider account")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userIdentityProviderService.delete(id);
        return ApiResponse.noContent();
    }
}
