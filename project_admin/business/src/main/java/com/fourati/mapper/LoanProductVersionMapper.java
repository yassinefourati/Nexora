package com.fourati.mapper;

import com.fourati.domain.LoanProductVersion;
import com.fourati.dto.request.CreateLoanProductVersionRequest;
import com.fourati.dto.response.LoanProductVersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanProductVersionMapper {

    @Mapping(target = "loanProductId", source = "loanProduct.id")
    LoanProductVersionResponse toResponse(LoanProductVersion loanProductVersion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanProduct", ignore = true)
    @Mapping(target = "status", ignore = true)
    LoanProductVersion toEntity(CreateLoanProductVersionRequest request);
}
