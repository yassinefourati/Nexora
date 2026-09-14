package com.fourati.access.internal.application;

import com.fourati.access.internal.domain.RoleMenu;
import com.fourati.access.CreateRoleMenuRequest;
import com.fourati.access.UpdateRoleMenuRequest;
import com.fourati.access.RoleMenuResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMenuMapper {

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "menuItemId", source = "menuItem.id")
    RoleMenuResponse toResponse(RoleMenu roleMenu);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    @Mapping(target = "canView", expression = "java(request.canView() == null || request.canView())")
    RoleMenu toEntity(CreateRoleMenuRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    void updateEntityFromRequest(UpdateRoleMenuRequest request, @MappingTarget RoleMenu roleMenu);
}
