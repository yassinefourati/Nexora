package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateMenuItemRequest;
import com.fourati.dto.request.UpdateMenuItemRequest;
import com.fourati.dto.response.MenuItemResponse;
import com.fourati.service.MenuItemService;
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
@RequestMapping(ApiConstants.VERSION + "/menu-items")
@Tag(name = "Menu Items", description = "Individual navigable items within a menu, optionally nested.")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new menu item")
    public ResponseEntity<ApiResponse<MenuItemResponse>> create(@Valid @RequestBody CreateMenuItemRequest request) {
        return ApiResponse.created(menuItemService.create(request), "Menu item created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu item by id")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(menuItemService.findById(id), "Menu item retrieved");
    }

    @GetMapping
    @Operation(summary = "List menu items", description = "Paginated list of menu items, or filtered by menuId/parentMenuItemId.")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> list(
            @RequestParam(required = false) UUID menuId,
            @RequestParam(required = false) UUID parentMenuItemId,
            @RequestParam(required = false, defaultValue = "false") boolean rootOnly,
            Pageable pageable) {
        if (menuId != null && rootOnly) {
            return ApiResponse.ok(menuItemService.findRootItemsByMenuId(menuId), "Menu items retrieved");
        }
        if (menuId != null) {
            return ApiResponse.ok(menuItemService.findByMenuId(menuId), "Menu items retrieved");
        }
        if (parentMenuItemId != null) {
            return ApiResponse.ok(menuItemService.findByParentMenuItemId(parentMenuItemId), "Menu items retrieved");
        }
        return ApiResponse.paged(menuItemService.findAll(pageable), "Menu items retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<ApiResponse<MenuItemResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateMenuItemRequest request) {
        return ApiResponse.ok(menuItemService.update(id, request), "Menu item updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        menuItemService.delete(id);
        return ApiResponse.noContent();
    }
}
