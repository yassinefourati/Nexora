package com.fourati.mapper;

import com.fourati.domain.DocumentVersion;
import com.fourati.dto.request.CreateDocumentVersionRequest;
import com.fourati.dto.response.DocumentVersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentVersionMapper {

    @Mapping(target = "documentId", source = "document.id")
    DocumentVersionResponse toResponse(DocumentVersion documentVersion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "document", ignore = true)
    DocumentVersion toEntity(CreateDocumentVersionRequest request);
}
