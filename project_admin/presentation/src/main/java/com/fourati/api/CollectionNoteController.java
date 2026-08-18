package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateCollectionNoteRequest;
import com.fourati.dto.response.CollectionNoteResponse;
import com.fourati.service.CollectionNoteService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/collection-notes")
@Tag(name = "Collection Notes", description = "Notes left by collectors on a collection case.")
public class CollectionNoteController {

    private final CollectionNoteService collectionNoteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a note to a collection case")
    public ResponseEntity<ApiResponse<CollectionNoteResponse>> create(
            @Valid @RequestBody CreateCollectionNoteRequest request) {
        CollectionNoteResponse created = collectionNoteService.create(request);
        return ApiResponse.created(created, "Collection note added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List notes for a collection case")
    public ResponseEntity<ApiResponse<List<CollectionNoteResponse>>> listByCase(
            @RequestParam UUID collectionCaseId) {
        return ApiResponse.ok(collectionNoteService.findByCollectionCaseId(collectionCaseId), "Collection notes retrieved");
    }
}
