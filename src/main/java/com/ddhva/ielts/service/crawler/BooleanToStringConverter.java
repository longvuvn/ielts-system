package com.ddhva.ielts.service.crawler;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) return null;
        return attribute ? "true" : "false";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return "true".equalsIgnoreCase(dbData);
    }
}
