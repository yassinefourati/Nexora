package com.fourati.mapper;

import com.fourati.domain.CustomerIdentification;
import com.fourati.dto.request.CreateCustomerIdentificationRequest;
import com.fourati.dto.response.CustomerIdentificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerIdentificationMapper {

    @Mapping(target = "customerId", source = "customer.id")
    CustomerIdentificationResponse toResponse(CustomerIdentification customerIdentification);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    CustomerIdentification toEntity(CreateCustomerIdentificationRequest request);
}
