package com.fourati.mapper;

import com.fourati.domain.LoanApplicationExpense;
import com.fourati.dto.request.CreateLoanApplicationExpenseRequest;
import com.fourati.dto.response.LoanApplicationExpenseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationExpenseMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    LoanApplicationExpenseResponse toResponse(LoanApplicationExpense loanApplicationExpense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    LoanApplicationExpense toEntity(CreateLoanApplicationExpenseRequest request);
}
