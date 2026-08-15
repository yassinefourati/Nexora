package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateRoleMenuRequest;
import com.fourati.dto.request.UpdateRoleMenuRequest;
import com.fourati.dto.response.RoleMenuResponse;
import com.fourati.service.RoleMenuService;
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
@RequestMapping(ApiConstants.VERSION + "/role-menus")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Role Menus", description = "Grants a role visibility into specific menu items.")
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    @PostMapping
    @Operation(summary = "Grant a role visibility into a menu item")
    public ResponseEntity<ApiResponse<RoleMenuResponse>> create(@Valid @RequestBody CreateRoleMenuRequest request) {
        return ApiResponse.created(roleMenuService.create(request), "Role menu link created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a role menu link by id")
    public ResponseEntity<ApiResponse<RoleMenuResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(roleMenuService.findById(id), "Role menu link retrieved");
    }

    @GetMapping
    @Operation(summary = "List role menu links", description = "Paginated, or filtered by roleId/menuItemId.")
    public ResponseEntity<ApiResponse<List<RoleMenuResponse>>> list(
            @RequestParam(required = false) UUID roleId,
            @RequestParam(required = false) UUID menuItemId,
            Pageable pageable) {
        if (roleId != null) {
            return ApiResponse.ok(roleMenuService.findByRoleId(roleId), "Role menu links retrieved");
        }
        if (menuItemId != null) {
            return ApiResponse.ok(roleMenuService.findByMenuItemId(menuItemId), "Role menu links retrieved");
        }
        return ApiResponse.paged(roleMenuService.findAll(pageable), "Role menu links retrieved");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role menu link's visibility flag")
    public ResponseEntity<ApiResponse<RoleMenuResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateRoleMenuRequest request) {
        return ApiResponse.ok(roleMenuService.update(id, request), "Role menu link updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke a role's visibility into a menu item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleMenuService.delete(id);
        return ApiResponse.noContent();
    }
}
