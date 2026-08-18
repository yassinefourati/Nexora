package com.fourati.mapper;

import com.fourati.domain.LoanDisbursement;
import com.fourati.dto.request.CreateLoanDisbursementRequest;
import com.fourati.dto.response.LoanDisbursementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanDisbursementMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "loanContractId", source = "loanContract.id")
    LoanDisbursementResponse toResponse(LoanDisbursement loanDisbursement);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "loanContract", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "initiatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "failedAt", ignore = true)
    LoanDisbursement toEntity(CreateLoanDisbursementRequest request);
}
