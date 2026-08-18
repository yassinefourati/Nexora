package com.fourati.mapper;

import com.fourati.domain.CustomerAddress;
import com.fourati.dto.request.CreateCustomerAddressRequest;
import com.fourati.dto.response.CustomerAddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerAddressMapper {

    @Mapping(target = "customerId", source = "customer.id")
    CustomerAddressResponse toResponse(CustomerAddress customerAddress);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    CustomerAddress toEntity(CreateCustomerAddressRequest request);
}
