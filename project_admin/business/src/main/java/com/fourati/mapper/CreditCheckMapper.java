package com.fourati.mapper;

import com.fourati.domain.CreditCheck;
import com.fourati.dto.response.CreditCheckResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditCheckMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "customerId", source = "customer.id")
    CreditCheckResponse toResponse(CreditCheck creditCheck);
}
