package com.fourati.mapper;

import com.fourati.domain.Team;
import com.fourati.dto.request.CreateTeamRequest;
import com.fourati.dto.request.UpdateTeamRequest;
import com.fourati.dto.response.TeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "departmentId", source = "department.id")
    TeamResponse toResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    Team toEntity(CreateTeamRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateEntityFromRequest(UpdateTeamRequest request, @MappingTarget Team team);
}
