package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUserNotificationRequest;
import com.fourati.dto.response.UserNotificationResponse;
import com.fourati.service.UserNotificationService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping(ApiConstants.VERSION + "/user-notifications")
@Tag(name = "User Notifications", description = "Track per-user delivery/read state of notifications")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Assign a notification to a user")
    public ResponseEntity<ApiResponse<UserNotificationResponse>> create(
            @Valid @RequestBody CreateUserNotificationRequest request) {
        return ApiResponse.created(userNotificationService.create(request), "User notification created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user notification by id")
    public ResponseEntity<ApiResponse<UserNotificationResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(userNotificationService.findById(id), "User notification retrieved");
    }

    @GetMapping
    @Operation(summary = "List user notifications, optionally filtered by user and read state")
    public ResponseEntity<ApiResponse<List<UserNotificationResponse>>> list(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Boolean read,
            Pageable pageable) {
        Page<UserNotificationResponse> page;
        if (userId != null && read != null) {
            page = userNotificationService.findByUserIdAndRead(userId, read, pageable);
        } else if (userId != null) {
            page = userNotificationService.findByUserId(userId, pageable);
        } else {
            page = userNotificationService.findAll(pageable);
        }
        return ApiResponse.paged(page, "User notifications retrieved");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Count unread notifications for a user")
    public ResponseEntity<ApiResponse<Long>> countUnread(@RequestParam UUID userId) {
        return ApiResponse.ok(userNotificationService.countUnread(userId), "Unread count retrieved");
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark a user notification as read")
    public ResponseEntity<ApiResponse<UserNotificationResponse>> markRead(@PathVariable UUID id) {
        return ApiResponse.ok(userNotificationService.markRead(id), "User notification marked as read");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a user notification assignment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userNotificationService.delete(id);
        return ApiResponse.noContent();
    }
}
