package com.fourati.identity.internal.domain;

import com.fourati.identity.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Maps {@link UserStatus} to the lowercase values the {@code users.status} check constraint allows. */
@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public UserStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UserStatus.fromValue(dbData);
    }
}
