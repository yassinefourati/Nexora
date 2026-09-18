package com.fourati.mapper;

import com.fourati.domain.LoanOfferStatusHistory;
import com.fourati.dto.response.LoanOfferStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanOfferStatusHistoryMapper {

    @Mapping(target = "loanOfferId", source = "loanOffer.id")
    LoanOfferStatusHistoryResponse toResponse(LoanOfferStatusHistory loanOfferStatusHistory);
}
