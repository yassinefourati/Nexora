package com.fourati.mapper;

import com.fourati.domain.Permission;
import com.fourati.dto.request.CreatePermissionRequest;
import com.fourati.dto.request.UpdatePermissionRequest;
import com.fourati.dto.response.PermissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    PermissionResponse toResponse(Permission permission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "code", ignore = true)
    Permission toEntity(CreatePermissionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "resource", ignore = true)
    @Mapping(target = "action", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntityFromRequest(UpdatePermissionRequest request, @MappingTarget Permission permission);
}
