package com.fourati.mapper;

import com.fourati.domain.LoanInstallment;
import com.fourati.dto.response.LoanInstallmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanInstallmentMapper {

    @Mapping(target = "loanAccountId", source = "loanAccount.id")
    LoanInstallmentResponse toResponse(LoanInstallment loanInstallment);
}
