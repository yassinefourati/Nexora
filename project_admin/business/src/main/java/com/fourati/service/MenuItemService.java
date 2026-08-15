package com.fourati.service;

import com.fourati.domain.Menu;
import com.fourati.domain.MenuItem;
import com.fourati.dto.request.CreateMenuItemRequest;
import com.fourati.dto.request.UpdateMenuItemRequest;
import com.fourati.dto.response.MenuItemResponse;
import com.fourati.mapper.MenuItemMapper;
import com.fourati.repository.MenuItemRepository;
import com.fourati.repository.MenuRepository;
import com.fourati.platform.audit.Audited;
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
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuRepository menuRepository;
    private final MenuItemMapper menuItemMapper;

    @Audited(action = "CREATE", description = "Create a new menu item")
    public MenuItemResponse create(CreateMenuItemRequest request) {
        MenuItem entity = menuItemMapper.toEntity(request);
        entity.setMenu(getMenuOrThrow(request.menuId()));
        if (request.parentMenuItemId() != null) {
            entity.setParentMenuItem(getMenuItemOrThrow(request.parentMenuItemId()));
        }
        MenuItem saved = menuItemRepository.save(entity);
        return menuItemMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findById(UUID id) {
        return menuItemMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<MenuItemResponse> findAll(Pageable pageable) {
        return menuItemRepository.findAll(pageable).map(menuItemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findByMenuId(UUID menuId) {
        return menuItemRepository.findByMenuId(menuId).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findByParentMenuItemId(UUID parentMenuItemId) {
        return menuItemRepository.findByParentMenuItemId(parentMenuItemId).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findRootItemsByMenuId(UUID menuId) {
        return menuItemRepository.findByMenuIdAndParentMenuItemIsNull(menuId).stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update a menu item")
    public MenuItemResponse update(UUID id, UpdateMenuItemRequest request) {
        MenuItem entity = getEntityOrThrow(id);
        menuItemMapper.updateEntityFromRequest(request, entity);
        if (request.parentMenuItemId() != null) {
            entity.setParentMenuItem(getMenuItemOrThrow(request.parentMenuItemId()));
        } else {
            entity.setParentMenuItem(null);
        }
        MenuItem saved = menuItemRepository.save(entity);
        return menuItemMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a menu item")
    public void delete(UUID id) {
        MenuItem entity = getEntityOrThrow(id);
        menuItemRepository.delete(entity);
    }

    private MenuItem getEntityOrThrow(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    private MenuItem getMenuItemOrThrow(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }

    private Menu getMenuOrThrow(UUID id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));
    }
}
