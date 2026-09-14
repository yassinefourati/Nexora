package com.fourati.content.internal.application;

import com.fourati.content.internal.domain.Attachment;
import com.fourati.content.CreateAttachmentRequest;
import com.fourati.content.AttachmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    AttachmentResponse toResponse(Attachment attachment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attachment toEntity(CreateAttachmentRequest request);
}
