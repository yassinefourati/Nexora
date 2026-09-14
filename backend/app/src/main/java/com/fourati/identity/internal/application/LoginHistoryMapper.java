package com.fourati.identity.internal.application;

import com.fourati.identity.internal.domain.LoginHistory;
import com.fourati.identity.LoginHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoginHistoryMapper {

    @Mapping(target = "userId", source = "user.id")
    LoginHistoryResponse toResponse(LoginHistory loginHistory);

}
