package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateEntityTagRequest;
import com.fourati.dto.response.EntityTagResponse;
import com.fourati.service.EntityTagService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/entity-tags")
@Tag(name = "Entity Tags", description = "Associate tags with arbitrary polymorphic entities.")
public class EntityTagController {

    private final EntityTagService entityTagService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Attach a tag to an entity")
    public ResponseEntity<ApiResponse<EntityTagResponse>> create(@Valid @RequestBody CreateEntityTagRequest request) {
        EntityTagResponse created = entityTagService.create(request);
        return ApiResponse.created(created, "Tag attached successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an entity-tag association by id")
    public ResponseEntity<ApiResponse<EntityTagResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(entityTagService.findById(id), "Entity tag retrieved");
    }

    @GetMapping
    @Operation(summary = "List tags attached to an entity, or entities attached to a tag")
    public ResponseEntity<ApiResponse<List<EntityTagResponse>>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) UUID tagId) {
        if (tagId != null) {
            return ApiResponse.ok(entityTagService.findByTag(tagId), "Entity tags retrieved");
        }
        return ApiResponse.ok(entityTagService.findByEntity(entityType, entityId), "Entity tags retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a tag from an entity")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        entityTagService.delete(id);
        return ApiResponse.noContent();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove all tags from an entity")
    public ResponseEntity<Void> deleteByEntity(@RequestParam String entityType, @RequestParam UUID entityId) {
        entityTagService.deleteByEntity(entityType, entityId);
        return ApiResponse.noContent();
    }
}
