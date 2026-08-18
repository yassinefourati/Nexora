package com.fourati.mapper;

import com.fourati.domain.UnderwritingCondition;
import com.fourati.dto.request.CreateUnderwritingConditionRequest;
import com.fourati.dto.response.UnderwritingConditionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwritingConditionMapper {

    @Mapping(target = "underwritingCaseId", source = "underwritingCase.id")
    UnderwritingConditionResponse toResponse(UnderwritingCondition underwritingCondition);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "underwritingCase", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "satisfiedAt", ignore = true)
    UnderwritingCondition toEntity(CreateUnderwritingConditionRequest request);
}
