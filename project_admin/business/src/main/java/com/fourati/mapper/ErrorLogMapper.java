package com.fourati.mapper;

import com.fourati.domain.ErrorLog;
import com.fourati.dto.request.CreateErrorLogRequest;
import com.fourati.dto.response.ErrorLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ErrorLogMapper {

    ErrorLogResponse toResponse(ErrorLog errorLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ErrorLog toEntity(CreateErrorLogRequest request);
}
