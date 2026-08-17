package com.fourati.mapper;

import com.fourati.domain.RiskAssessmentFactor;
import com.fourati.dto.response.RiskAssessmentFactorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskAssessmentFactorMapper {

    @Mapping(target = "riskAssessmentId", source = "riskAssessment.id")
    RiskAssessmentFactorResponse toResponse(RiskAssessmentFactor riskAssessmentFactor);
}
