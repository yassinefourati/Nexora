package com.fourati.identity.internal.application;

import com.fourati.identity.CreateUserRequest;
import com.fourati.identity.UpdateUserRequest;
import com.fourati.identity.UserResponse;
import com.fourati.identity.internal.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "superuser", source = "superuser", defaultValue = "false")
    @Mapping(target = "status", source = "status", defaultValue = "ACTIVE")
    User toEntity(CreateUserRequest request);

    /** Profile fields only; the status transition is the use case's decision ({@code User.changeStatus}). */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
