package com.fourati.mapper;

import com.fourati.domain.Document;
import com.fourati.dto.request.CreateDocumentRequest;
import com.fourati.dto.response.DocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DocumentMapper {

    DocumentResponse toResponse(Document document);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    Document toEntity(CreateDocumentRequest request);
}
