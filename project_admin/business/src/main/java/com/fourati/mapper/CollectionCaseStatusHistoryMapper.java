package com.fourati.mapper;

import com.fourati.domain.CollectionCaseStatusHistory;
import com.fourati.dto.response.CollectionCaseStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionCaseStatusHistoryMapper {

    @Mapping(target = "collectionCaseId", source = "collectionCase.id")
    CollectionCaseStatusHistoryResponse toResponse(CollectionCaseStatusHistory collectionCaseStatusHistory);
}
