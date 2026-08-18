package com.fourati.mapper;

import com.fourati.domain.ContractSignature;
import com.fourati.dto.request.CreateContractSignatureRequest;
import com.fourati.dto.response.ContractSignatureResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContractSignatureMapper {

    @Mapping(target = "loanContractId", source = "loanContract.id")
    ContractSignatureResponse toResponse(ContractSignature contractSignature);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanContract", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "declineReason", ignore = true)
    @Mapping(target = "requestedAt", ignore = true)
    @Mapping(target = "signedAt", ignore = true)
    @Mapping(target = "declinedAt", ignore = true)
    ContractSignature toEntity(CreateContractSignatureRequest request);
}
