package com.fourati.mapper;

import com.fourati.domain.LoanAccount;
import com.fourati.dto.request.CreateLoanAccountRequest;
import com.fourati.dto.response.LoanAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanAccountMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "loanDisbursementId", source = "loanDisbursement.id")
    LoanAccountResponse toResponse(LoanAccount loanAccount);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "loanDisbursement", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "principalAmount", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "termMonths", ignore = true)
    @Mapping(target = "outstandingPrincipal", ignore = true)
    @Mapping(target = "openedAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    LoanAccount toEntity(CreateLoanAccountRequest request);
}
