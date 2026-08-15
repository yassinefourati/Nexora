package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateNotificationRequest;
import com.fourati.dto.request.UpdateNotificationRequest;
import com.fourati.dto.response.NotificationResponse;
import com.fourati.service.NotificationService;
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
@RequestMapping(ApiConstants.VERSION + "/notifications")
@Tag(name = "Notifications", description = "Manage notification content; per-recipient delivery/read state lives under user-notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ApiResponse.created(notificationService.create(request), "Notification created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.findById(id), "Notification retrieved");
    }

    @GetMapping
    @Operation(summary = "List notifications, optionally filtered by channel or template")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) UUID templateId,
            Pageable pageable) {
        Page<NotificationResponse> page;
        if (channel != null) {
            page = notificationService.findByChannel(channel, pageable);
        } else if (templateId != null) {
            page = notificationService.findByTemplateId(templateId, pageable);
        } else {
            page = notificationService.findAll(pageable);
        }
        return ApiResponse.paged(page, "Notifications retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a notification's content")
    public ResponseEntity<ApiResponse<NotificationResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateNotificationRequest request) {
        return ApiResponse.ok(notificationService.update(id, request), "Notification updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ApiResponse.noContent();
    }
}
