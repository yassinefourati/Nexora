package com.fourati.mapper;

import com.fourati.domain.FraudAlert;
import com.fourati.dto.response.FraudAlertResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FraudAlertMapper {

    @Mapping(target = "fraudCheckId", source = "fraudCheck.id")
    FraudAlertResponse toResponse(FraudAlert fraudAlert);
}
