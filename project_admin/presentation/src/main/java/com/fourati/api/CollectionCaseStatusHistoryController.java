package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.response.CollectionCaseStatusHistoryResponse;
import com.fourati.service.CollectionCaseStatusHistoryService;
import com.fourati.platform.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/collection-case-status-history")
@Tag(name = "Collection Case Status History", description = "Read-only audit trail of collection case status transitions.")
public class CollectionCaseStatusHistoryController {

    private final CollectionCaseStatusHistoryService collectionCaseStatusHistoryService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List status history for a collection case")
    public ResponseEntity<ApiResponse<List<CollectionCaseStatusHistoryResponse>>> listByCase(
            @RequestParam UUID collectionCaseId) {
        return ApiResponse.ok(collectionCaseStatusHistoryService.findByCollectionCaseId(collectionCaseId), "Collection case status history retrieved");
    }
}
