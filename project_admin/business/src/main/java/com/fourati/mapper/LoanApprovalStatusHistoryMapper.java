package com.fourati.mapper;

import com.fourati.domain.LoanApprovalStatusHistory;
import com.fourati.dto.response.LoanApprovalStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApprovalStatusHistoryMapper {

    @Mapping(target = "loanApprovalId", source = "loanApproval.id")
    LoanApprovalStatusHistoryResponse toResponse(LoanApprovalStatusHistory loanApprovalStatusHistory);
}
