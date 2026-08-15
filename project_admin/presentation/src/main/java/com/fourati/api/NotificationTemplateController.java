package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateNotificationTemplateRequest;
import com.fourati.dto.request.UpdateNotificationTemplateRequest;
import com.fourati.dto.response.NotificationTemplateResponse;
import com.fourati.service.NotificationTemplateService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/notification-templates")
@Tag(name = "Notification Templates", description = "Manage reusable templates used to render notifications")
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(
            @Valid @RequestBody CreateNotificationTemplateRequest request) {
        return ApiResponse.created(notificationTemplateService.create(request), "Notification template created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification template by id")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(notificationTemplateService.findById(id), "Notification template retrieved");
    }

    @GetMapping
    @Operation(summary = "List notification templates")
    public ResponseEntity<ApiResponse<java.util.List<NotificationTemplateResponse>>> list(Pageable pageable) {
        Page<NotificationTemplateResponse> page = notificationTemplateService.findAll(pageable);
        return ApiResponse.paged(page, "Notification templates retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a notification template")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateNotificationTemplateRequest request) {
        return ApiResponse.ok(notificationTemplateService.update(id, request), "Notification template updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a notification template")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationTemplateService.delete(id);
        return ApiResponse.noContent();
    }
}
