package com.fourati.mapper;

import com.fourati.domain.KycCheck;
import com.fourati.dto.request.CreateKycCheckRequest;
import com.fourati.dto.response.KycCheckResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycCheckMapper {

    @Mapping(target = "kycCaseId", source = "kycCase.id")
    KycCheckResponse toResponse(KycCheck kycCheck);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "kycCase", ignore = true)
    KycCheck toEntity(CreateKycCheckRequest request);
}
