package com.fourati.content.internal.application;


import com.fourati.content.internal.domain.EntityTag;
import com.fourati.content.CreateEntityTagRequest;
import com.fourati.content.EntityTagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EntityTagMapper {

    @Mapping(target = "tagId", source = "tag.id")
    EntityTagResponse toResponse(EntityTag entityTag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tag", ignore = true)
    EntityTag toEntity(CreateEntityTagRequest request);
}
