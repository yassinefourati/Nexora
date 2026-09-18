package com.fourati.mapper;

import com.fourati.domain.LoanApprovalCondition;
import com.fourati.dto.request.CreateLoanApprovalConditionRequest;
import com.fourati.dto.response.LoanApprovalConditionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanApprovalConditionMapper {

    @Mapping(target = "loanApprovalId", source = "loanApproval.id")
    LoanApprovalConditionResponse toResponse(LoanApprovalCondition loanApprovalCondition);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "loanApproval", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "satisfiedAt", ignore = true)
    LoanApprovalCondition toEntity(CreateLoanApprovalConditionRequest request);
}
