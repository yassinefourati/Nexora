package com.fourati.mapper;

import com.fourati.domain.DocumentRequirement;
import com.fourati.dto.request.CreateDocumentRequirementRequest;
import com.fourati.dto.response.DocumentRequirementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentRequirementMapper {

    @Mapping(target = "loanProductId", source = "loanProduct.id")
    DocumentRequirementResponse toResponse(DocumentRequirement documentRequirement);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    DocumentRequirement toEntity(CreateDocumentRequirementRequest request);
}
