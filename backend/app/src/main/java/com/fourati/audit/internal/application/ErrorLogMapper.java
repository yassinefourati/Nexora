package com.fourati.audit.internal.application;

import com.fourati.audit.internal.domain.ErrorLog;
import com.fourati.audit.CreateErrorLogRequest;
import com.fourati.audit.ErrorLogResponse;
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
