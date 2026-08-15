package com.fourati.mapper;

import com.fourati.domain.EntityTag;
import com.fourati.dto.request.CreateEntityTagRequest;
import com.fourati.dto.response.EntityTagResponse;
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
