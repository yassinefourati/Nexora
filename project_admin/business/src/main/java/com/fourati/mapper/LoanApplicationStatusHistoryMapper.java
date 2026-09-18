package com.fourati.mapper;

import com.fourati.domain.LoanApplicationStatusHistory;
import com.fourati.dto.response.LoanApplicationStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationStatusHistoryMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    LoanApplicationStatusHistoryResponse toResponse(LoanApplicationStatusHistory loanApplicationStatusHistory);
}
