package com.fourati.mapper;

import com.fourati.domain.LoanDisbursementStatusHistory;
import com.fourati.dto.response.LoanDisbursementStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanDisbursementStatusHistoryMapper {

    @Mapping(target = "loanDisbursementId", source = "loanDisbursement.id")
    LoanDisbursementStatusHistoryResponse toResponse(LoanDisbursementStatusHistory loanDisbursementStatusHistory);
}
