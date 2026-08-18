package com.fourati.mapper;

import com.fourati.domain.LoanRepayment;
import com.fourati.dto.request.CreateLoanRepaymentRequest;
import com.fourati.dto.response.LoanRepaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanRepaymentMapper {

    @Mapping(target = "loanAccountId", source = "loanAccount.id")
    @Mapping(target = "loanInstallmentId", source = "loanInstallment.id")
    LoanRepaymentResponse toResponse(LoanRepayment loanRepayment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanAccount", ignore = true)
    @Mapping(target = "loanInstallment", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "referenceNumber", ignore = true)
    @Mapping(target = "failureReason", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "failedAt", ignore = true)
    LoanRepayment toEntity(CreateLoanRepaymentRequest request);
}
