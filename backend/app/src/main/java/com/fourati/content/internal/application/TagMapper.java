package com.fourati.content.internal.application;

import com.fourati.content.internal.domain.Tag;
import com.fourati.content.CreateTagRequest;
import com.fourati.content.UpdateTagRequest;
import com.fourati.content.TagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagResponse toResponse(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Tag toEntity(CreateTagRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateTagRequest request, @MappingTarget Tag tag);
}
