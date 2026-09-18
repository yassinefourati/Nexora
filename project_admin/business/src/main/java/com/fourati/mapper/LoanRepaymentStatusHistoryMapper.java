package com.fourati.mapper;

import com.fourati.domain.LoanRepaymentStatusHistory;
import com.fourati.dto.response.LoanRepaymentStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanRepaymentStatusHistoryMapper {

    @Mapping(target = "loanRepaymentId", source = "loanRepayment.id")
    LoanRepaymentStatusHistoryResponse toResponse(LoanRepaymentStatusHistory loanRepaymentStatusHistory);
}
