package com.fourati.access.internal.application;

import com.fourati.access.internal.domain.UserRole;
import com.fourati.access.CreateUserRoleRequest;
import com.fourati.access.UserRoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserRoleMapper {

    @Mapping(target = "roleId", source = "role.id")
    UserRoleResponse toResponse(UserRole userRole);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    UserRole toEntity(CreateUserRoleRequest request);
}
