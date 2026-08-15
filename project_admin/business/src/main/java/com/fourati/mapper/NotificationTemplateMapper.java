package com.fourati.mapper;

import com.fourati.domain.NotificationTemplate;
import com.fourati.dto.request.CreateNotificationTemplateRequest;
import com.fourati.dto.request.UpdateNotificationTemplateRequest;
import com.fourati.dto.response.NotificationTemplateResponse;
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
