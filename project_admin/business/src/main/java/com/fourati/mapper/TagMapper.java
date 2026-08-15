package com.fourati.mapper;

import com.fourati.domain.Tag;
import com.fourati.dto.request.CreateTagRequest;
import com.fourati.dto.request.UpdateTagRequest;
import com.fourati.dto.response.TagResponse;
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
