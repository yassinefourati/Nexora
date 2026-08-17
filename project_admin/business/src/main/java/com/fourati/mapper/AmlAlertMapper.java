package com.fourati.mapper;

import com.fourati.domain.AmlAlert;
import com.fourati.dto.request.CreateAmlAlertRequest;
import com.fourati.dto.response.AmlAlertResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AmlAlertMapper {

    @Mapping(target = "amlScreeningId", source = "amlScreening.id")
    AmlAlertResponse toResponse(AmlAlert amlAlert);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "amlScreening", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    AmlAlert toEntity(CreateAmlAlertRequest request);
}
