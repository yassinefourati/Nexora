package com.fourati.mapper;

import com.fourati.domain.MenuItem;
import com.fourati.dto.request.CreateMenuItemRequest;
import com.fourati.dto.request.UpdateMenuItemRequest;
import com.fourati.dto.response.MenuItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "menuId", source = "menu.id")
    @Mapping(target = "parentMenuItemId", source = "parentMenuItem.id")
    MenuItemResponse toResponse(MenuItem menuItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "parentMenuItem", ignore = true)
    @Mapping(target = "sortOrder", expression = "java(request.sortOrder() == null ? 0 : request.sortOrder())")
    @Mapping(target = "active", expression = "java(request.active() == null || request.active())")
    MenuItem toEntity(CreateMenuItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "parentMenuItem", ignore = true)
    @Mapping(target = "sortOrder", expression = "java(request.sortOrder() == null ? menuItem.getSortOrder() : request.sortOrder())")
    @Mapping(target = "active", expression = "java(request.active() == null ? menuItem.isActive() : request.active())")
    void updateEntityFromRequest(UpdateMenuItemRequest request, @MappingTarget MenuItem menuItem);
}
