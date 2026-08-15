package com.fourati.mapper;

import com.fourati.domain.AuditLog;
import com.fourati.dto.request.CreateAuditLogRequest;
import com.fourati.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "userId", source = "user.id")
    AuditLogResponse toResponse(AuditLog auditLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    AuditLog toEntity(CreateAuditLogRequest request);
}
