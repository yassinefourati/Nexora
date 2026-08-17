package com.fourati.mapper;

import com.fourati.domain.KycCase;
import com.fourati.dto.request.CreateKycCaseRequest;
import com.fourati.dto.response.KycCaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycCaseMapper {

    @Mapping(target = "customerId", source = "customer.id")
    KycCaseResponse toResponse(KycCase kycCase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "initiatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    KycCase toEntity(CreateKycCaseRequest request);
}
