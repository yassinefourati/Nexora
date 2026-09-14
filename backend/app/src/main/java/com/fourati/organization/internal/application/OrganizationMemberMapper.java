package com.fourati.organization.internal.application;

import com.fourati.organization.internal.domain.OrganizationMember;
import com.fourati.organization.CreateOrganizationMemberRequest;
import com.fourati.organization.OrganizationMemberResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrganizationMemberMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "teamId", source = "team.id")
    OrganizationMemberResponse toResponse(OrganizationMember organizationMember);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    OrganizationMember toEntity(CreateOrganizationMemberRequest request);
}
