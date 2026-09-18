package com.fourati.mapper;

import com.fourati.domain.LoanProductFeeRule;
import com.fourati.dto.request.CreateLoanProductFeeRuleRequest;
import com.fourati.dto.response.LoanProductFeeRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanProductFeeRuleMapper {

    @Mapping(target = "loanProductId", source = "loanProduct.id")
    LoanProductFeeRuleResponse toResponse(LoanProductFeeRule loanProductFeeRule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    LoanProductFeeRule toEntity(CreateLoanProductFeeRuleRequest request);
}
