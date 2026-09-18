package com.fourati.mapper;

import com.fourati.domain.LoanApproval;
import com.fourati.dto.request.CreateLoanApprovalRequest;
import com.fourati.dto.response.LoanApprovalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApprovalMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "underwritingCaseId", source = "underwritingCase.id")
    LoanApprovalResponse toResponse(LoanApproval loanApproval);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "loanApplication", ignore = true)
    @Mapping(target = "underwritingCase", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedAmount", ignore = true)
    @Mapping(target = "approvedTermMonths", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    LoanApproval toEntity(CreateLoanApprovalRequest request);
}
