package com.fourati.service;

import com.fourati.domain.Menu;
import com.fourati.dto.request.CreateMenuRequest;
import com.fourati.dto.request.UpdateMenuRequest;
import com.fourati.dto.response.MenuResponse;
import com.fourati.mapper.MenuMapper;
import com.fourati.repository.MenuRepository;
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

    @Audited(action = "UPDATE", description = "Update a menu")
    public MenuResponse update(UUID id, UpdateMenuRequest request) {
        Menu entity = getEntityOrThrow(id);
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
