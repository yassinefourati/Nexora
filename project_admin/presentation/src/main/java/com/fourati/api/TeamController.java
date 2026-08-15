package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateTeamRequest;
import com.fourati.dto.request.UpdateTeamRequest;
import com.fourati.dto.response.TeamResponse;
import com.fourati.service.TeamService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.VERSION + "/teams")
@Tag(name = "Teams", description = "Manage teams within an organization, optionally scoped to a department.")
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new team")
    public ResponseEntity<ApiResponse<TeamResponse>> create(@Valid @RequestBody CreateTeamRequest request) {
        TeamResponse created = teamService.create(request);
        return ApiResponse.created(created, "Team created successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a team by id")
    public ResponseEntity<ApiResponse<TeamResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(teamService.findById(id), "Team retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List teams (paginated), optionally filtered by organization or department")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> list(Pageable pageable,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID departmentId) {
        if (organizationId != null) {
            return ApiResponse.ok(teamService.findByOrganizationId(organizationId), "Teams retrieved");
        }
        if (departmentId != null) {
            return ApiResponse.ok(teamService.findByDepartmentId(departmentId), "Teams retrieved");
        }
        return ApiResponse.paged(teamService.findAll(pageable), "Teams retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a team")
    public ResponseEntity<ApiResponse<TeamResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateTeamRequest request) {
        return ApiResponse.ok(teamService.update(id, request), "Team updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a team")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        teamService.delete(id);
        return ApiResponse.noContent();
    }
}
