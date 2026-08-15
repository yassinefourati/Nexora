package com.fourati.mapper;

import com.fourati.domain.MetadataKv;
import com.fourati.dto.request.CreateMetadataKvRequest;
import com.fourati.dto.request.UpdateMetadataKvRequest;
import com.fourati.dto.response.MetadataKvResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MetadataKvMapper {

    MetadataKvResponse toResponse(MetadataKv metadataKv);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MetadataKv toEntity(CreateMetadataKvRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "entityType", ignore = true)
    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "key", ignore = true)
    void updateEntityFromRequest(UpdateMetadataKvRequest request, @MappingTarget MetadataKv metadataKv);
}
