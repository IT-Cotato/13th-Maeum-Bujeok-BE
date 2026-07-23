package com.maumbujeok.backend.global.ai.emotion;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReportEmotionConverter implements AttributeConverter<ReportEmotion, String> {
    @Override
    public String convertToDatabaseColumn(ReportEmotion attribute) {
        return attribute == null ? null : attribute.getLabel();
    }

    @Override
    public ReportEmotion convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReportEmotion.fromLabel(dbData);
    }
}
