package com.fourati.mapper;

import com.fourati.domain.LoanApplicationCoApplicant;
import com.fourati.dto.request.CreateLoanApplicationCoApplicantRequest;
import com.fourati.dto.response.LoanApplicationCoApplicantResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApplicationCoApplicantMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "customerId", source = "customer.id")
    LoanApplicationCoApplicantResponse toResponse(LoanApplicationCoApplicant loanApplicationCoApplicant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "customer", ignore = true)
    LoanApplicationCoApplicant toEntity(CreateLoanApplicationCoApplicantRequest request);
}
