package com.fourati.mapper;

import com.fourati.domain.UnderwritingCase;
import com.fourati.dto.request.CreateUnderwritingCaseRequest;
import com.fourati.dto.response.UnderwritingCaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwritingCaseMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    UnderwritingCaseResponse toResponse(UnderwritingCase underwritingCase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "decision", ignore = true)
    @Mapping(target = "decisionReason", ignore = true)
    @Mapping(target = "approvedAmount", ignore = true)
    @Mapping(target = "approvedTermMonths", ignore = true)
    @Mapping(target = "decidedAt", ignore = true)
    UnderwritingCase toEntity(CreateUnderwritingCaseRequest request);
}
