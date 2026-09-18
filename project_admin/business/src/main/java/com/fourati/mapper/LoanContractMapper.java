package com.fourati.mapper;

import com.fourati.domain.LoanContract;
import com.fourati.dto.request.CreateLoanContractRequest;
import com.fourati.dto.response.LoanContractResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanContractMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "loanOfferId", source = "loanOffer.id")
    LoanContractResponse toResponse(LoanContract loanContract);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "loanOffer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "principalAmount", ignore = true)
    @Mapping(target = "termMonths", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "documentUrl", ignore = true)
    @Mapping(target = "finalizedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    LoanContract toEntity(CreateLoanContractRequest request);
}
