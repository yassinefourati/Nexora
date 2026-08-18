package com.fourati.mapper;

import com.fourati.domain.LoanApplication;
import com.fourati.dto.request.CreateLoanApplicationRequest;
import com.fourati.dto.request.UpdateLoanApplicationRequest;
import com.fourati.dto.response.LoanApplicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LoanApplicationMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "loanProductId", source = "loanProduct.id")
    LoanApplicationResponse toResponse(LoanApplication loanApplication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    LoanApplication toEntity(CreateLoanApplicationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    void updateEntityFromRequest(UpdateLoanApplicationRequest request, @MappingTarget LoanApplication loanApplication);
}
