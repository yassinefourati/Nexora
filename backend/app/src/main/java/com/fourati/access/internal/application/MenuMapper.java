package com.fourati.access.internal.application;

import com.fourati.access.internal.domain.Menu;
import com.fourati.access.CreateMenuRequest;
import com.fourati.access.UpdateMenuRequest;
import com.fourati.access.MenuResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    MenuResponse toResponse(Menu menu);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "active", expression = "java(request.active() == null || request.active())")
    Menu toEntity(CreateMenuRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "active", expression = "java(request.active() == null ? menu.isActive() : request.active())")
    void updateEntityFromRequest(UpdateMenuRequest request, @MappingTarget Menu menu);
}
