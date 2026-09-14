package com.fourati.audit.internal.application;

import com.fourati.audit.internal.domain.AuditLog;
import com.fourati.audit.CreateAuditLogRequest;
import com.fourati.audit.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuditLog toEntity(CreateAuditLogRequest request);
}
