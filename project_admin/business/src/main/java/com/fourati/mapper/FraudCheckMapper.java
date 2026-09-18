package com.fourati.mapper;

import com.fourati.domain.FraudCheck;
import com.fourati.dto.response.FraudCheckResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FraudCheckMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    FraudCheckResponse toResponse(FraudCheck fraudCheck);
}
