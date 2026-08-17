package com.fourati.mapper;

import com.fourati.domain.AmlScreening;
import com.fourati.dto.request.CreateAmlScreeningRequest;
import com.fourati.dto.response.AmlScreeningResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AmlScreeningMapper {

    @Mapping(target = "kycCaseId", source = "kycCase.id")
    AmlScreeningResponse toResponse(AmlScreening amlScreening);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "kycCase", ignore = true)
    AmlScreening toEntity(CreateAmlScreeningRequest request);
}
