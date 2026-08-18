package com.fourati.mapper;

import com.fourati.domain.UnderwritingStatusHistory;
import com.fourati.dto.response.UnderwritingStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnderwritingStatusHistoryMapper {

    @Mapping(target = "underwritingCaseId", source = "underwritingCase.id")
    UnderwritingStatusHistoryResponse toResponse(UnderwritingStatusHistory underwritingStatusHistory);
}
