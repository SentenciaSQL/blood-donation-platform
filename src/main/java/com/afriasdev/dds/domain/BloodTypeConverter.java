package com.afriasdev.dds.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BloodTypeConverter implements AttributeConverter<BloodType, String> {

    @Override
    public String convertToDatabaseColumn(BloodType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public BloodType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BloodType.fromCode(dbData);
    }
}
