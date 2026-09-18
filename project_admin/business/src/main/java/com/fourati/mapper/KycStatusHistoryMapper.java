package com.fourati.mapper;

import com.fourati.domain.KycStatusHistory;
import com.fourati.dto.response.KycStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface KycStatusHistoryMapper {

    @Mapping(target = "kycCaseId", source = "kycCase.id")
    KycStatusHistoryResponse toResponse(KycStatusHistory kycStatusHistory);
}
