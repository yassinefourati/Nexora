package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateTagRequest;
import com.fourati.dto.request.UpdateTagRequest;
import com.fourati.dto.response.TagResponse;
import com.fourati.service.TagService;
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
@RequestMapping(ApiConstants.VERSION + "/tags")
@Tag(name = "Tags", description = "Manage labels that can be attached to arbitrary entities.")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new tag")
    public ResponseEntity<ApiResponse<TagResponse>> create(@Valid @RequestBody CreateTagRequest request) {
        TagResponse created = tagService.create(request);
        return ApiResponse.created(created, "Tag created successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a tag by id")
    public ResponseEntity<ApiResponse<TagResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(tagService.findById(id), "Tag retrieved");
    }

    @GetMapping
    @Operation(summary = "List tags (paginated)")
    public ResponseEntity<ApiResponse<List<TagResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(tagService.findAll(pageable), "Tags retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update a tag")
    public ResponseEntity<ApiResponse<TagResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateTagRequest request) {
        return ApiResponse.ok(tagService.update(id, request), "Tag updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a tag")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagService.delete(id);
        return ApiResponse.noContent();
    }
}
