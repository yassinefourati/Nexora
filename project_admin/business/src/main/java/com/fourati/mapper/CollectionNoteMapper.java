package com.fourati.mapper;

import com.fourati.domain.CollectionNote;
import com.fourati.dto.request.CreateCollectionNoteRequest;
import com.fourati.dto.response.CollectionNoteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionNoteMapper {

    @Mapping(target = "collectionCaseId", source = "collectionCase.id")
    CollectionNoteResponse toResponse(CollectionNote collectionNote);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "collectionCase", ignore = true)
    CollectionNote toEntity(CreateCollectionNoteRequest request);
}
