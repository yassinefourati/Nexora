package com.fourati.api;

import com.fourati.common.ApiConstants;
import com.fourati.dto.request.CreateMenuRequest;
import com.fourati.dto.request.UpdateMenuRequest;
import com.fourati.dto.response.MenuResponse;
import com.fourati.service.MenuService;
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
@RequestMapping(ApiConstants.VERSION + "/menus")
@Tag(name = "Menus", description = "Global navigation menus.")
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new menu")
    public ResponseEntity<ApiResponse<MenuResponse>> create(@Valid @RequestBody CreateMenuRequest request) {
        return ApiResponse.created(menuService.create(request), "Menu created");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu by id")
    public ResponseEntity<ApiResponse<MenuResponse>> getById(@PathVariable UUID id) {
        return ApiResponse.ok(menuService.findById(id), "Menu retrieved");
    }

    @GetMapping
    @Operation(summary = "List menus", description = "Paginated list of menus.")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> list(Pageable pageable) {
        return ApiResponse.paged(menuService.findAll(pageable), "Menus retrieved");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a menu")
    public ResponseEntity<ApiResponse<MenuResponse>> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateMenuRequest request) {
        return ApiResponse.ok(menuService.update(id, request), "Menu updated");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a menu")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        menuService.delete(id);
        return ApiResponse.noContent();
    }
}
