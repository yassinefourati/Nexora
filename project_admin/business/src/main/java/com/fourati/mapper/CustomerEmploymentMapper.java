package com.fourati.mapper;

import com.fourati.domain.CustomerEmployment;
import com.fourati.dto.request.CreateCustomerEmploymentRequest;
import com.fourati.dto.response.CustomerEmploymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerEmploymentMapper {

    @Mapping(target = "customerId", source = "customer.id")
    CustomerEmploymentResponse toResponse(CustomerEmployment customerEmployment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    CustomerEmployment toEntity(CreateCustomerEmploymentRequest request);
}
