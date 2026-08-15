package com.fourati.mapper;

import com.fourati.domain.User;
import com.fourati.dto.request.CreateUserRequest;
import com.fourati.dto.request.UpdateUserRequest;
import com.fourati.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "superuser", source = "superuser", defaultValue = "false")
    @Mapping(target = "status", source = "status", defaultValue = "active")
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "passwordChangedAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User user);
}
