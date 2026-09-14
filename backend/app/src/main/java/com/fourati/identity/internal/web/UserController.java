package com.fourati.identity.internal.web;

import com.fourati.shared.security.Authorize;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UpdateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.application.ActivateUser;
import com.fourati.identity.internal.application.CreateUser;
import com.fourati.identity.internal.application.DeleteUser;
import com.fourati.identity.internal.application.FindUser;
import com.fourati.identity.internal.application.SearchUsers;
import com.fourati.identity.internal.application.SuspendUser;
import com.fourati.identity.internal.application.UpdateUser;
import com.fourati.platform.web.ApiResponse;
import com.fourati.shared.api.ApiConstants;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Thin adapter: every method binds the request, calls one use case and wraps the result (ADR 0004). */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/users")
@Tag(name = "Users", description = "Manage application user accounts.")
public class UserController {

    private final CreateUser createUser;
    private final UpdateUser updateUser;
    private final DeleteUser deleteUser;
    private final SuspendUser suspendUser;
    private final ActivateUser activateUser;
    private final FindUser findUser;
    private final SearchUsers searchUsers;

    @PostMapping
    @PreAuthorize(Authorize.USER_WRITE)
    @Operation(summary = "Pre-provision a user before their first sign-in")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.created(createUser.execute(request), "User created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(findUser.execute(id), "User retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List users (paginated); q searches username/email/name, status filters exactly")
    public ResponseEntity<ApiResponse<List<UserResponse>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ApiResponse.paged(searchUsers.execute(q, status, pageable), "Users retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize(Authorize.USER_EDIT)
    @Operation(summary = "Update a user's profile, superuser flag or status")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(updateUser.execute(id, request), "User updated successfully");
    }

    @PostMapping("/{id}:suspend")
    @PreAuthorize(Authorize.USER_EDIT)
    @Operation(summary = "Suspend a user: keeps rows and grants, blocks new role grants until reactivated")
    public ResponseEntity<ApiResponse<UserResponse>> suspend(@PathVariable UUID id) {
        return ApiResponse.ok(suspendUser.execute(id), "User suspended");
    }

    @PostMapping("/{id}:activate")
    @PreAuthorize(Authorize.USER_EDIT)
    @Operation(summary = "Reactivate a suspended, inactive or pending user")
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable UUID id) {
        return ApiResponse.ok(activateUser.execute(id), "User activated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Authorize.USER_DELETE)
    @Operation(summary = "Soft-delete a user")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUser.execute(id);
        return ApiResponse.noContent();
    }
}
