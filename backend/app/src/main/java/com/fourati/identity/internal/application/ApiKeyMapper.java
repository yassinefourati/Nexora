package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.ApiKey;
import com.fourati.identity.CreateApiKeyRequest;
import com.fourati.identity.UpdateApiKeyRequest;
import com.fourati.identity.ApiKeyCreatedResponse;
import com.fourati.identity.ApiKeyResponse;
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
