package com.fourati.access.internal.application;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.access.internal.domain.MenuItem;
import com.fourati.access.internal.domain.Role;
import com.fourati.access.internal.domain.RoleMenu;
import com.fourati.access.CreateRoleMenuRequest;
import com.fourati.access.UpdateRoleMenuRequest;
import com.fourati.access.RoleMenuResponse;
import com.fourati.access.internal.persistence.MenuItemRepository;
import com.fourati.access.internal.persistence.RoleMenuRepository;
import com.fourati.access.internal.persistence.RoleRepository;
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
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final RoleRepository roleRepository;
    private final MenuItemRepository menuItemRepository;
    private final RoleMenuMapper roleMenuMapper;

    @Audited(action = "CREATE", description = "Grant a role visibility into a menu item")
    public RoleMenuResponse create(CreateRoleMenuRequest request) {
        if (roleMenuRepository.existsByRoleIdAndMenuItemId(request.roleId(), request.menuItemId())) {
            throw new ConflictException(
                    "Role menu link already exists for roleId=" + request.roleId() + ", menuItemId=" + request.menuItemId());
        }
        RoleMenu entity = roleMenuMapper.toEntity(request);
        entity.setRole(getRoleOrThrow(request.roleId()));
        entity.setMenuItem(getMenuItemOrThrow(request.menuItemId()));
        RoleMenu saved = roleMenuRepository.save(entity);
        return roleMenuMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoleMenuResponse findById(UUID id) {
        return roleMenuMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<RoleMenuResponse> findAll(Pageable pageable) {
        return roleMenuRepository.findAll(pageable).map(roleMenuMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<RoleMenuResponse> findByRoleId(UUID roleId) {
        return roleMenuRepository.findByRoleId(roleId).stream()
                .map(roleMenuMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleMenuResponse> findByMenuItemId(UUID menuItemId) {
        return roleMenuRepository.findByMenuItemId(menuItemId).stream()
                .map(roleMenuMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update role menu visibility")
    public RoleMenuResponse update(UUID id, UpdateRoleMenuRequest request) {
        RoleMenu entity = getEntityOrThrow(id);
        VersionCheck.check(RoleMenu.class, id, entity.getVersion(), request.version());
        roleMenuMapper.updateEntityFromRequest(request, entity);
        RoleMenu saved = roleMenuRepository.save(entity);
        return roleMenuMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Revoke a role's visibility into a menu item")
    public void delete(UUID id) {
        RoleMenu entity = getEntityOrThrow(id);
        roleMenuRepository.delete(entity);
    }

    private RoleMenu getEntityOrThrow(UUID id) {
        return roleMenuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoleMenu", id));
    }

    private Role getRoleOrThrow(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
    }

    private MenuItem getMenuItemOrThrow(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
    }
}
