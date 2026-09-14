package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.UserIdentityProvider;
import com.fourati.identity.CreateUserIdentityProviderRequest;
import com.fourati.identity.UserIdentityProviderResponse;
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
