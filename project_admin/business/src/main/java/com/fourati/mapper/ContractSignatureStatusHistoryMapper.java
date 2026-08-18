package com.fourati.mapper;

import com.fourati.domain.ContractSignatureStatusHistory;
import com.fourati.dto.response.ContractSignatureStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContractSignatureStatusHistoryMapper {

    @Mapping(target = "contractSignatureId", source = "contractSignature.id")
    ContractSignatureStatusHistoryResponse toResponse(ContractSignatureStatusHistory contractSignatureStatusHistory);
}
