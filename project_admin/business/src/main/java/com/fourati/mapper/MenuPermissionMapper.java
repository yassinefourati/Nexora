package com.fourati.mapper;

import com.fourati.domain.MenuPermission;
import com.fourati.dto.request.CreateMenuPermissionRequest;
import com.fourati.dto.response.MenuPermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuPermissionMapper {

    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "permissionId", source = "permission.id")
    MenuPermissionResponse toResponse(MenuPermission menuPermission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    @Mapping(target = "permission", ignore = true)
    MenuPermission toEntity(CreateMenuPermissionRequest request);
}
