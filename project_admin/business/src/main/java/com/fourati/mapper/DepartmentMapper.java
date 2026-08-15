package com.fourati.mapper;

import com.fourati.domain.Department;
import com.fourati.dto.request.CreateDepartmentRequest;
import com.fourati.dto.request.UpdateDepartmentRequest;
import com.fourati.dto.response.DepartmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "parentDepartmentId", source = "parentDepartment.id")
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    Department toEntity(CreateDepartmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    void updateEntityFromRequest(UpdateDepartmentRequest request, @MappingTarget Department department);
}
