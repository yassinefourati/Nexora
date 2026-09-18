package com.fourati.mapper;

import com.fourati.domain.LoanContractStatusHistory;
import com.fourati.dto.response.LoanContractStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanContractStatusHistoryMapper {

    @Mapping(target = "loanContractId", source = "loanContract.id")
    LoanContractStatusHistoryResponse toResponse(LoanContractStatusHistory loanContractStatusHistory);
}
