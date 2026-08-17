package com.fourati.mapper;

import com.fourati.domain.CreditReport;
import com.fourati.dto.response.CreditReportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditReportMapper {

    @Mapping(target = "creditCheckId", source = "creditCheck.id")
    CreditReportResponse toResponse(CreditReport creditReport);
}
