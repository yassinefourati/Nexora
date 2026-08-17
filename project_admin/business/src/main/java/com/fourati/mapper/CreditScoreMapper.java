package com.fourati.mapper;

import com.fourati.domain.CreditScore;
import com.fourati.dto.response.CreditScoreResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditScoreMapper {

    @Mapping(target = "creditCheckId", source = "creditCheck.id")
    CreditScoreResponse toResponse(CreditScore creditScore);
}
