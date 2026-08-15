package com.fourati.mapper;

import com.fourati.domain.Setting;
import com.fourati.dto.request.CreateSettingRequest;
import com.fourati.dto.request.UpdateSettingRequest;
import com.fourati.dto.response.SettingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SettingMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    SettingResponse toResponse(Setting entity);

    @Mapping(target = "organization", ignore = true)
    Setting toEntity(CreateSettingRequest request);

    void updateEntityFromRequest(UpdateSettingRequest request, @MappingTarget Setting entity);
}
