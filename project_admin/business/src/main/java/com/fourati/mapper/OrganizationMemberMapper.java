package com.fourati.mapper;

import com.fourati.domain.OrganizationMember;
import com.fourati.dto.request.CreateOrganizationMemberRequest;
import com.fourati.dto.response.OrganizationMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMemberMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "teamId", source = "team.id")
    OrganizationMemberResponse toResponse(OrganizationMember organizationMember);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    OrganizationMember toEntity(CreateOrganizationMemberRequest request);
}
