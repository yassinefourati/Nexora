package com.fourati.organization.internal.domain;

import com.fourati.organization.OrganizationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Maps {@link OrganizationStatus} to the lowercase values the {@code organizations.status} check constraint allows. */
@Converter(autoApply = true)
public class OrganizationStatusConverter implements AttributeConverter<OrganizationStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrganizationStatus attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public OrganizationStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : OrganizationStatus.fromValue(dbData);
    }
}
