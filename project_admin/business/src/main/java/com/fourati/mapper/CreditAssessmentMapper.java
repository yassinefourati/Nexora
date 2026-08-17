package com.fourati.mapper;

import com.fourati.domain.CreditAssessment;
import com.fourati.dto.response.CreditAssessmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditAssessmentMapper {

    @Mapping(target = "creditCheckId", source = "creditCheck.id")
    CreditAssessmentResponse toResponse(CreditAssessment creditAssessment);
}
