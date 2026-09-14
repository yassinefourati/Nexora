package com.fourati.audit.internal.application;

import com.fourati.audit.internal.domain.SystemEvent;
import com.fourati.audit.CreateSystemEventRequest;
import com.fourati.audit.SystemEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SystemEventMapper {

    SystemEventResponse toResponse(SystemEvent systemEvent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SystemEvent toEntity(CreateSystemEventRequest request);
}
