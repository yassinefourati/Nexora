package com.fourati.mapper;

import com.fourati.domain.LoanProductEligibilityRule;
import com.fourati.dto.request.CreateLoanProductEligibilityRuleRequest;
import com.fourati.dto.response.LoanProductEligibilityRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanProductEligibilityRuleMapper {

    @Mapping(target = "loanProductId", source = "loanProduct.id")
    LoanProductEligibilityRuleResponse toResponse(LoanProductEligibilityRule loanProductEligibilityRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    LoanProductEligibilityRule toEntity(CreateLoanProductEligibilityRuleRequest request);
}
