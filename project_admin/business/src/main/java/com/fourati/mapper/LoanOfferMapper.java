package com.fourati.mapper;

import com.fourati.domain.LoanOffer;
import com.fourati.dto.request.CreateLoanOfferRequest;
import com.fourati.dto.response.LoanOfferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanOfferMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "loanApprovalId", source = "loanApproval.id")
    LoanOfferResponse toResponse(LoanOffer loanOffer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "loanApproval", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "offeredAmount", ignore = true)
    @Mapping(target = "offeredTermMonths", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "declineReason", ignore = true)
    @Mapping(target = "issuedAt", ignore = true)
    @Mapping(target = "acceptedAt", ignore = true)
    @Mapping(target = "declinedAt", ignore = true)
    LoanOffer toEntity(CreateLoanOfferRequest request);
}
