package com.fourati.mapper;

import com.fourati.domain.UserIdentityProvider;
import com.fourati.dto.request.CreateUserIdentityProviderRequest;
import com.fourati.dto.response.UserIdentityProviderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserIdentityProviderMapper {

    @Mapping(target = "userId", source = "user.id")
    UserIdentityProviderResponse toResponse(UserIdentityProvider userIdentityProvider);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "linkedAt", ignore = true)
    UserIdentityProvider toEntity(CreateUserIdentityProviderRequest request);
}
