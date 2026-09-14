package com.fourati.identity.internal.web;

import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.application.FindUser;
import com.fourati.platform.error.ResourceNotFoundException;
import com.fourati.platform.security.CurrentUser;
import com.fourati.platform.web.ApiResponse;
import com.fourati.shared.api.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /me}: the current user's backend row, resolved from the
 * {@link CurrentUser} principal rather than a path parameter. This is how the
 * frontend learns who it is talking on behalf of. {@code /me/permissions} is
 * served by the access module.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/me")
@Tag(name = "Me", description = "Endpoints scoped to the current authenticated user.")
public class MeController {

    private final FindUser findUser;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the current user's backend record, resolved at authentication time")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal CurrentUser me) {
        if (me == null || me.userId() == null) {
            throw new ResourceNotFoundException("User", me != null ? me.subject() : "anonymous");
        }
        return ApiResponse.ok(findUser.execute(me.userId()), "Current user retrieved");
    }
}
