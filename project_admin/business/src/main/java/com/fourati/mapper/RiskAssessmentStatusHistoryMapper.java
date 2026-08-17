package com.fourati.mapper;

import com.fourati.domain.RiskAssessmentStatusHistory;
import com.fourati.dto.response.RiskAssessmentStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RiskAssessmentStatusHistoryMapper {

    @Mapping(target = "riskAssessmentId", source = "riskAssessment.id")
    RiskAssessmentStatusHistoryResponse toResponse(RiskAssessmentStatusHistory riskAssessmentStatusHistory);
}
