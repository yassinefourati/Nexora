package com.fourati.mapper;

import com.fourati.domain.Session;
import com.fourati.dto.request.CreateSessionRequest;
import com.fourati.dto.request.UpdateSessionRequest;
import com.fourati.dto.response.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "userId", source = "user.id")
    SessionResponse toResponse(Session session);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    Session toEntity(CreateSessionRequest request);

    void updateEntityFromRequest(UpdateSessionRequest request, @MappingTarget Session session);
}
