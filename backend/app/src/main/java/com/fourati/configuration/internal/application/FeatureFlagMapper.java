package com.fourati.configuration.internal.application;

import com.fourati.configuration.internal.domain.FeatureFlag;
import com.fourati.configuration.CreateFeatureFlagRequest;
import com.fourati.configuration.UpdateFeatureFlagRequest;
import com.fourati.configuration.FeatureFlagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeatureFlagMapper {

    FeatureFlagResponse toResponse(FeatureFlag entity);

    FeatureFlag toEntity(CreateFeatureFlagRequest request);

    void updateEntityFromRequest(UpdateFeatureFlagRequest request, @MappingTarget FeatureFlag entity);
}
