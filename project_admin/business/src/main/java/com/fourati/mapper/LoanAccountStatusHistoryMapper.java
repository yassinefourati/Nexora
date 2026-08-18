package com.fourati.mapper;

import com.fourati.domain.LoanAccountStatusHistory;
import com.fourati.dto.response.LoanAccountStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanAccountStatusHistoryMapper {

    @Mapping(target = "loanAccountId", source = "loanAccount.id")
    LoanAccountStatusHistoryResponse toResponse(LoanAccountStatusHistory loanAccountStatusHistory);
}
