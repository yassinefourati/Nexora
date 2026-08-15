package com.fourati.mapper;

import com.fourati.domain.LoginHistory;
import com.fourati.dto.request.CreateLoginHistoryRequest;
import com.fourati.dto.request.UpdateLoginHistoryRequest;
import com.fourati.dto.response.LoginHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LoginHistoryMapper {

    @Mapping(target = "userId", source = "user.id")
    LoginHistoryResponse toResponse(LoginHistory loginHistory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "logoutAt", ignore = true)
    LoginHistory toEntity(CreateLoginHistoryRequest request);

    void updateEntityFromRequest(UpdateLoginHistoryRequest request, @MappingTarget LoginHistory loginHistory);
}
