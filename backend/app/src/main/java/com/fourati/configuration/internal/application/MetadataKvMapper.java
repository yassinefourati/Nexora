package com.fourati.configuration.internal.application;

import com.fourati.configuration.internal.domain.MetadataKv;
import com.fourati.configuration.CreateMetadataKvRequest;
import com.fourati.configuration.UpdateMetadataKvRequest;
import com.fourati.configuration.MetadataKvResponse;
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
