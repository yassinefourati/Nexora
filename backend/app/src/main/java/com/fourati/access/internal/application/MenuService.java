package com.fourati.access.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.access.internal.domain.Menu;
import com.fourati.access.CreateMenuRequest;
import com.fourati.access.UpdateMenuRequest;
import com.fourati.access.MenuResponse;
import com.fourati.access.internal.persistence.MenuRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    @Audited(action = "CREATE", description = "Create a new menu")
    public MenuResponse create(CreateMenuRequest request) {
        if (menuRepository.existsByCode(request.code())) {
            throw new ConflictException("Menu already exists with code: " + request.code());
        }
        Menu entity = menuMapper.toEntity(request);
        Menu saved = menuRepository.save(entity);
        return menuMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MenuResponse findById(UUID id) {
        return menuMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<MenuResponse> findAll(Pageable pageable) {
        return menuRepository.findAll(pageable).map(menuMapper::toResponse);
    }

    /** Server-side search over name, code, description (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<MenuResponse> search(String q, Pageable pageable) {
        return menuRepository.findAll(TextSearch.<Menu>contains(q, "name", "code", "description"), pageable).map(menuMapper::toResponse);
    }

    @Audited(action = "UPDATE", description = "Update a menu")
    public MenuResponse update(UUID id, UpdateMenuRequest request) {
        Menu entity = getEntityOrThrow(id);
        VersionCheck.check(Menu.class, id, entity.getVersion(), request.version());
        menuMapper.updateEntityFromRequest(request, entity);
        Menu saved = menuRepository.save(entity);
        return menuMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Delete a menu")
    public void delete(UUID id) {
        Menu entity = getEntityOrThrow(id);
        menuRepository.delete(entity);
    }

    private Menu getEntityOrThrow(UUID id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));
    }
}
