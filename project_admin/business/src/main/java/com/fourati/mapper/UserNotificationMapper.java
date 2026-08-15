package com.fourati.mapper;

import com.fourati.domain.UserNotification;
import com.fourati.dto.request.CreateUserNotificationRequest;
import com.fourati.dto.response.UserNotificationResponse;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserNotificationMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "notificationId", source = "notification.id")
    UserNotificationResponse toResponse(UserNotification entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "notification", ignore = true)
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    UserNotification toEntity(CreateUserNotificationRequest request);
}
