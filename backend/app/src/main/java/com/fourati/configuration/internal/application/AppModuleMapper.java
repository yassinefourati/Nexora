package com.fourati.configuration.internal.application;

import com.fourati.configuration.internal.domain.AppModule;
import com.fourati.configuration.CreateAppModuleRequest;
import com.fourati.configuration.UpdateAppModuleRequest;
import com.fourati.configuration.AppModuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppModuleMapper {

    AppModuleResponse toResponse(AppModule entity);

    AppModule toEntity(CreateAppModuleRequest request);

    void updateEntityFromRequest(UpdateAppModuleRequest request, @MappingTarget AppModule entity);
}
