package com.fourati.service;

import com.fourati.domain.MenuItem;
import com.fourati.domain.MenuPermission;
import com.fourati.domain.Permission;
import com.fourati.dto.request.CreateMenuPermissionRequest;
import com.fourati.dto.response.MenuPermissionResponse;
import com.fourati.mapper.MenuPermissionMapper;
import com.fourati.repository.MenuItemRepository;
import com.fourati.repository.MenuPermissionRepository;
import com.fourati.repository.PermissionRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuPermissionService {

    private final MenuPermissionRepository menuPermissionRepository;
    private final MenuItemRepository menuItemRepository;
    private final PermissionRepository permissionRepository;
    private final MenuPermissionMapper menuPermissionMapper;

    @Audited(action = "CREATE", description = "Associate a permission required to view/use a menu item")
    public MenuPermissionResponse create(CreateMenuPermissionRequest request) {
        if (menuPermissionRepository.existsByMenuItemIdAndPermissionId(request.menuItemId(), request.permissionId())) {
            throw new ConflictException(
                    "Menu permission link already exists for menuItemId=" + request.menuItemId()
                            + ", permissionId=" + request.permissionId());
        }
        MenuPermission entity = menuPermissionMapper.toEntity(request);
        entity.setMenuItem(getMenuItemOrThrow(request.menuItemId()));
        entity.setPermission(getPermissionOrThrow(request.permissionId()));
        MenuPermission saved = menuPermissionRepository.save(entity);
        return menuPermissionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MenuPermissionResponse findById(UUID id) {
        return menuPermissionMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<MenuPermissionResponse> findAll(Pageable pageable) {
        return menuPermissionRepository.findAll(pageable).map(menuPermissionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MenuPermissionResponse> findByMenuItemId(UUID menuItemId) {
        return menuPermissionRepository.findByMenuItemId(menuItemId).stream()
                .map(menuPermissionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuPermissionResponse> findByPermissionId(UUID permissionId) {
        return menuPermissionRepository.findByPermissionId(permissionId).stream()
                .map(menuPermissionMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a permission requirement from a menu item")
    public void delete(UUID id) {
        MenuPermission entity = getEntityOrThrow(id);
        menuPermissionRepository.delete(entity);
    }

    private MenuPermission getEntityOrThrow(UUID id) {
        return menuPermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuPermission", id));
    }

    private MenuItem getMenuItemOrThrow(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    private Permission getPermissionOrThrow(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", id));
    }
}
