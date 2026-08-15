package com.fourati.mapper;

import com.fourati.domain.SystemEvent;
import com.fourati.dto.request.CreateSystemEventRequest;
import com.fourati.dto.response.SystemEventResponse;
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
