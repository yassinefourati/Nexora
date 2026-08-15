package com.fourati.mapper;

import com.fourati.domain.ApiKey;
import com.fourati.dto.request.CreateApiKeyRequest;
import com.fourati.dto.request.UpdateApiKeyRequest;
import com.fourati.dto.response.ApiKeyCreatedResponse;
import com.fourati.dto.response.ApiKeyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    @Mapping(target = "userId", source = "user.id")
    ApiKeyResponse toResponse(ApiKey apiKey);

    @Mapping(target = "userId", source = "apiKey.user.id")
    @Mapping(target = "secret", source = "secret")
    ApiKeyCreatedResponse toCreatedResponse(ApiKey apiKey, String secret);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "keyHash", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    ApiKey toEntity(CreateApiKeyRequest request);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateApiKeyRequest request, @MappingTarget ApiKey apiKey);
}
