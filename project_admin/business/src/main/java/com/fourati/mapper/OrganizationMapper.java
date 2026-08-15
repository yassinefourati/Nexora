package com.fourati.mapper;

import com.fourati.domain.Organization;
import com.fourati.dto.request.CreateOrganizationRequest;
import com.fourati.dto.request.UpdateOrganizationRequest;
import com.fourati.dto.response.OrganizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {

    @Mapping(target = "parentOrganizationId", source = "parentOrganization.id")
    OrganizationResponse toResponse(Organization organization);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "parentOrganization", ignore = true)
    Organization toEntity(CreateOrganizationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "parentOrganization", ignore = true)
    void updateEntityFromRequest(UpdateOrganizationRequest request, @MappingTarget Organization organization);
}
