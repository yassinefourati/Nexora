package com.fourati.mapper;

import com.fourati.domain.FeatureFlag;
import com.fourati.dto.request.CreateFeatureFlagRequest;
import com.fourati.dto.request.UpdateFeatureFlagRequest;
import com.fourati.dto.response.FeatureFlagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeatureFlagMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    FeatureFlagResponse toResponse(FeatureFlag entity);

    @Mapping(target = "organization", ignore = true)
    FeatureFlag toEntity(CreateFeatureFlagRequest request);

    void updateEntityFromRequest(UpdateFeatureFlagRequest request, @MappingTarget FeatureFlag entity);
}
