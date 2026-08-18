package com.fourati.mapper;

import com.fourati.domain.CollectionCase;
import com.fourati.dto.request.CreateCollectionCaseRequest;
import com.fourati.dto.response.CollectionCaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionCaseMapper {

    @Mapping(target = "loanAccountId", source = "loanAccount.id")
    @Mapping(target = "loanInstallmentId", source = "loanInstallment.id")
    CollectionCaseResponse toResponse(CollectionCase collectionCase);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanAccount", ignore = true)
    @Mapping(target = "loanInstallment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "stage", ignore = true)
    @Mapping(target = "overdueAmount", ignore = true)
    @Mapping(target = "resolutionNotes", ignore = true)
    @Mapping(target = "openedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    CollectionCase toEntity(CreateCollectionCaseRequest request);
}
