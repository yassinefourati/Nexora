package com.fourati.mapper;

import com.fourati.domain.Attachment;
import com.fourati.dto.request.CreateAttachmentRequest;
import com.fourati.dto.response.AttachmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    @Mapping(target = "uploadedBy", source = "uploadedBy.id")
    AttachmentResponse toResponse(Attachment attachment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    Attachment toEntity(CreateAttachmentRequest request);
}
