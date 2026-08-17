package com.fourati.mapper;

import com.fourati.domain.RiskAssessment;
import com.fourati.dto.response.RiskAssessmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskAssessmentMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    RiskAssessmentResponse toResponse(RiskAssessment riskAssessment);
}
