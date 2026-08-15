package com.fourati.mapper;

import com.fourati.domain.AuthLog;
import com.fourati.dto.request.CreateAuthLogRequest;
import com.fourati.dto.response.AuthLogResponse;
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
