package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateUnderwritingNoteRequest;
import com.fourati.dto.response.UnderwritingNoteResponse;
import com.fourati.service.UnderwritingNoteService;
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
@RequestMapping(ApiConstants.VERSION + "/underwriting-notes")
@Tag(name = "Underwriting Notes", description = "Manage notes left by underwriters on a case.")
public class UnderwritingNoteController {

    private final UnderwritingNoteService underwritingNoteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a note to an underwriting case")
    public ResponseEntity<ApiResponse<UnderwritingNoteResponse>> create(
            @Valid @RequestBody CreateUnderwritingNoteRequest request) {
        UnderwritingNoteResponse created = underwritingNoteService.create(request);
        return ApiResponse.created(created, "Underwriting note added successfully");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List notes for an underwriting case")
    public ResponseEntity<ApiResponse<List<UnderwritingNoteResponse>>> listByCase(
            @RequestParam UUID underwritingCaseId) {
        return ApiResponse.ok(underwritingNoteService.findByUnderwritingCaseId(underwritingCaseId), "Underwriting notes retrieved");
    }
}
