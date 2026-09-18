package com.fourati.mapper;

import com.fourati.domain.LoanProduct;
import com.fourati.dto.request.CreateLoanProductRequest;
import com.fourati.dto.request.UpdateLoanProductRequest;
import com.fourati.dto.response.LoanProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LoanProductMapper {

    LoanProductResponse toResponse(LoanProduct loanProduct);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    LoanProduct toEntity(CreateLoanProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromRequest(UpdateLoanProductRequest request, @MappingTarget LoanProduct loanProduct);
}
