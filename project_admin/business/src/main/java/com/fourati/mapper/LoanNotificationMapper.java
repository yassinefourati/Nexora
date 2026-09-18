package com.fourati.mapper;

import com.fourati.domain.LoanNotification;
import com.fourati.dto.response.LoanNotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanNotificationMapper {

    @Mapping(target = "loanApplicationId", source = "loanApplication.id")
    @Mapping(target = "notificationId", source = "notification.id")
    @Mapping(target = "title", source = "notification.title")
    @Mapping(target = "body", source = "notification.body")
    @Mapping(target = "channel", source = "notification.channel")
    LoanNotificationResponse toResponse(LoanNotification loanNotification);
}
