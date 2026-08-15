package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateMenuPermissionRequest;
import com.fourati.dto.response.MenuPermissionResponse;
import com.fourati.service.MenuPermissionService;
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
@RequestMapping(ApiConstants.VERSION + "/menu-permissions")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Menu Permissions", description = "Associates permissions required to view/use a menu item.")
public class MenuPermissionController {

    private final MenuPermissionService menuPermissionService;

    @PostMapping
    @Operation(summary = "Associate a permission with a menu item")
    public ResponseEntity<ApiResponse<MenuPermissionResponse>> create(
            @Valid @RequestBody CreateMenuPermissionRequest request) {
        return ApiResponse.created(menuPermissionService.create(request), "Menu permission link created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu permission link by id")
    public ResponseEntity<ApiResponse<MenuPermissionResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(menuPermissionService.findById(id), "Menu permission link retrieved");
    }

    @GetMapping
    @Operation(summary = "List menu permission links", description = "Paginated, or filtered by menuItemId/permissionId.")
    public ResponseEntity<ApiResponse<List<MenuPermissionResponse>>> list(
            @RequestParam(required = false) UUID menuItemId,
            @RequestParam(required = false) UUID permissionId,
            Pageable pageable) {
        if (menuItemId != null) {
            return ApiResponse.ok(menuPermissionService.findByMenuItemId(menuItemId), "Menu permission links retrieved");
        }
        if (permissionId != null) {
            return ApiResponse.ok(menuPermissionService.findByPermissionId(permissionId), "Menu permission links retrieved");
        }
        return ApiResponse.paged(menuPermissionService.findAll(pageable), "Menu permission links retrieved");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a permission requirement from a menu item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        menuPermissionService.delete(id);
        return ApiResponse.noContent();
    }
}
