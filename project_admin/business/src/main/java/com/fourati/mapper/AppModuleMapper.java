package com.fourati.mapper;

import com.fourati.domain.AppModule;
import com.fourati.dto.request.CreateAppModuleRequest;
import com.fourati.dto.request.UpdateAppModuleRequest;
import com.fourati.dto.response.AppModuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AppModuleMapper {

    AppModuleResponse toResponse(AppModule entity);

    AppModule toEntity(CreateAppModuleRequest request);

    void updateEntityFromRequest(UpdateAppModuleRequest request, @MappingTarget AppModule entity);
}
