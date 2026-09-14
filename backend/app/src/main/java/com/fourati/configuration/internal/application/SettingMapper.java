package com.fourati.configuration.internal.application;

import com.fourati.configuration.internal.domain.Setting;
import com.fourati.configuration.CreateSettingRequest;
import com.fourati.configuration.UpdateSettingRequest;
import com.fourati.configuration.SettingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SettingMapper {

    SettingResponse toResponse(Setting entity);

    Setting toEntity(CreateSettingRequest request);

    void updateEntityFromRequest(UpdateSettingRequest request, @MappingTarget Setting entity);
}
