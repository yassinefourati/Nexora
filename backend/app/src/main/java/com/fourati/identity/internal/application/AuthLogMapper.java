package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.AuthLog;
import com.fourati.identity.CreateAuthLogRequest;
import com.fourati.identity.AuthLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthLogMapper {

    @Mapping(target = "userId", source = "user.id")
    AuthLogResponse toResponse(AuthLog authLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    AuthLog toEntity(CreateAuthLogRequest request);
}
