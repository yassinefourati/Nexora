package com.fourati.mapper;

import com.fourati.domain.CustomerConsent;
import com.fourati.dto.request.CreateCustomerConsentRequest;
import com.fourati.dto.response.CustomerConsentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerConsentMapper {

    @Mapping(target = "customerId", source = "customer.id")
    CustomerConsentResponse toResponse(CustomerConsent customerConsent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "grantedAt", ignore = true)
    @Mapping(target = "revokedAt", ignore = true)
    CustomerConsent toEntity(CreateCustomerConsentRequest request);
}
