package com.fourati.mapper;

import com.fourati.domain.LoanProductRateRule;
import com.fourati.dto.request.CreateLoanProductRateRuleRequest;
import com.fourati.dto.response.LoanProductRateRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanProductRateRuleMapper {

    @Mapping(target = "loanProductId", source = "loanProduct.id")
    LoanProductRateRuleResponse toResponse(LoanProductRateRule loanProductRateRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    LoanProductRateRule toEntity(CreateLoanProductRateRuleRequest request);
}
