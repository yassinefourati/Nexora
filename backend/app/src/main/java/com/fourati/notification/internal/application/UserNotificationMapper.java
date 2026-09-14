package com.fourati.notification.internal.application;


import com.fourati.notification.internal.domain.UserNotification;
import com.fourati.notification.CreateUserNotificationRequest;
import com.fourati.notification.UserNotificationResponse;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserNotificationMapper {

    @Mapping(target = "notificationId", source = "notification.id")
    UserNotificationResponse toResponse(UserNotification entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notification", ignore = true)
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    UserNotification toEntity(CreateUserNotificationRequest request);
}
