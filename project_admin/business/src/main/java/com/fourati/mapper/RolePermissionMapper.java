package com.fourati.mapper;

import com.fourati.domain.RolePermission;
import com.fourati.dto.request.CreateRolePermissionRequest;
import com.fourati.dto.response.RolePermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RolePermissionMapper {

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "permissionId", source = "permission.id")
    RolePermissionResponse toResponse(RolePermission rolePermission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "permission", ignore = true)
    @Mapping(target = "conditions", source = "conditions", defaultValue = "{}")
    RolePermission toEntity(CreateRolePermissionRequest request);
}
