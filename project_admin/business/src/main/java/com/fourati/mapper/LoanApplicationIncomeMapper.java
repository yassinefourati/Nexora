package com.fourati.mapper;

import com.fourati.domain.LoanApplicationIncome;
import com.fourati.dto.request.CreateLoanApplicationIncomeRequest;
import com.fourati.dto.response.LoanApplicationIncomeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationIncomeMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    LoanApplicationIncomeResponse toResponse(LoanApplicationIncome loanApplicationIncome);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    LoanApplicationIncome toEntity(CreateLoanApplicationIncomeRequest request);
}
