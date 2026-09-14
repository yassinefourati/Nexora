package com.fourati.notification.internal.application;

import com.fourati.notification.internal.domain.NotificationTemplate;
import com.fourati.notification.CreateNotificationTemplateRequest;
import com.fourati.notification.UpdateNotificationTemplateRequest;
import com.fourati.notification.NotificationTemplateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NotificationTemplateMapper {

    NotificationTemplateResponse toResponse(NotificationTemplate entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NotificationTemplate toEntity(CreateNotificationTemplateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateNotificationTemplateRequest request, @MappingTarget NotificationTemplate entity);
}
