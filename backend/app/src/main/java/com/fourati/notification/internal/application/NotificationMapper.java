package com.fourati.notification.internal.application;

import com.fourati.notification.internal.domain.Notification;
import com.fourati.notification.CreateNotificationRequest;
import com.fourati.notification.UpdateNotificationRequest;
import com.fourati.notification.NotificationResponse;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "templateId", source = "template.id")
    NotificationResponse toResponse(Notification entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "template", ignore = true)
    Notification toEntity(CreateNotificationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "template", ignore = true)
    void updateEntityFromRequest(UpdateNotificationRequest request, @MappingTarget Notification entity);
}
