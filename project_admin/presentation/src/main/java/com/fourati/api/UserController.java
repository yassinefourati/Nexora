package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUserRequest;
import com.fourati.dto.request.UpdateUserRequest;
import com.fourati.dto.response.UserResponse;
import com.fourati.service.UserService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/users")
@Tag(name = "Users", description = "Manage application user accounts.")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);
        return ApiResponse.created(created, "User created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userService.findById(id), "User retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List users (paginated)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(userService.findAll(pageable), "Users retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok(userService.update(id, request), "User updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a user")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ApiResponse.noContent();
    }
}
