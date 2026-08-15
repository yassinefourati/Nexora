package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateAttachmentRequest;
import com.fourati.dto.response.AttachmentResponse;
import com.fourati.service.AttachmentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/attachments")
@Tag(name = "Attachments", description = "Files attached to arbitrary polymorphic entities.")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Register a new attachment")
    public ResponseEntity<ApiResponse<AttachmentResponse>> create(@Valid @RequestBody CreateAttachmentRequest request) {
        AttachmentResponse created = attachmentService.create(request);
        return ApiResponse.created(created, "Attachment created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an attachment by id")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(attachmentService.findById(id), "Attachment retrieved");
    }

    @GetMapping
    @Operation(summary = "List attachments for an entity")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> listByEntity(
            @RequestParam String entityType, @RequestParam UUID entityId) {
        return ApiResponse.ok(attachmentService.findByEntity(entityType, entityId), "Attachments retrieved");
    }

    @GetMapping("/by-uploader/{uploadedById}")
    @Operation(summary = "List attachments uploaded by a user (paginated)")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> listByUploader(@PathVariable UUID uploadedById,
            Pageable pageable) {
        return ApiResponse.paged(attachmentService.findByUploadedBy(uploadedById, pageable), "Attachments retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        attachmentService.delete(id);
        return ApiResponse.noContent();
    }
}
