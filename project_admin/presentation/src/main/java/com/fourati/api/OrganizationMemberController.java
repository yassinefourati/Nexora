package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateOrganizationMemberRequest;
import com.fourati.dto.response.OrganizationMemberResponse;
import com.fourati.service.OrganizationMemberService;
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
@RequestMapping(ApiConstants.VERSION + "/organization-members")
@Tag(name = "Organization Members", description = "Manage membership of users within organizations.")
public class OrganizationMemberController {

    private final OrganizationMemberService organizationMemberService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a member to an organization")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> create(
            @Valid @RequestBody CreateOrganizationMemberRequest request) {
        OrganizationMemberResponse created = organizationMemberService.create(request);
        return ApiResponse.created(created, "Organization member added successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get an organization membership by id")
    public ResponseEntity<ApiResponse<OrganizationMemberResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(organizationMemberService.findById(id), "Organization member retrieved");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List organization memberships (paginated), optionally filtered by organization, user, department, or team")
    public ResponseEntity<ApiResponse<List<OrganizationMemberResponse>>> list(Pageable pageable,
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID teamId) {
        if (organizationId != null) {
            return ApiResponse.ok(organizationMemberService.findByOrganizationId(organizationId), "Organization members retrieved");
        }
        if (userId != null) {
            return ApiResponse.ok(organizationMemberService.findByUserId(userId), "Organization members retrieved");
        }
        if (departmentId != null) {
            return ApiResponse.ok(organizationMemberService.findByDepartmentId(departmentId), "Organization members retrieved");
        }
        if (teamId != null) {
            return ApiResponse.ok(organizationMemberService.findByTeamId(teamId), "Organization members retrieved");
        }
        return ApiResponse.paged(organizationMemberService.findAll(pageable), "Organization members retrieved");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a member from an organization")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organizationMemberService.delete(id);
        return ApiResponse.noContent();
    }
}
