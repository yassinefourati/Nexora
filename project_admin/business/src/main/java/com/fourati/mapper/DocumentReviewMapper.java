package com.fourati.mapper;

import com.fourati.domain.DocumentReview;
import com.fourati.dto.response.DocumentReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentReviewMapper {

    @Mapping(target = "documentId", source = "document.id")
    DocumentReviewResponse toResponse(DocumentReview documentReview);
}
