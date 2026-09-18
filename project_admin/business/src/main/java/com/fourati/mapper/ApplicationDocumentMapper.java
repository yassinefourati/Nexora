package com.fourati.mapper;

import com.fourati.domain.ApplicationDocument;
import com.fourati.dto.request.CreateApplicationDocumentRequest;
import com.fourati.dto.response.ApplicationDocumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationDocumentMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "documentId", source = "document.id")
    ApplicationDocumentResponse toResponse(ApplicationDocument applicationDocument);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "document", ignore = true)
    ApplicationDocument toEntity(CreateApplicationDocumentRequest request);
}
