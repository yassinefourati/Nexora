package com.fourati.mapper;

import com.fourati.domain.CreditCheckStatusHistory;
import com.fourati.dto.response.CreditCheckStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditCheckStatusHistoryMapper {

    @Mapping(target = "creditCheckId", source = "creditCheck.id")
    CreditCheckStatusHistoryResponse toResponse(CreditCheckStatusHistory creditCheckStatusHistory);
}
