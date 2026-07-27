package com.maumbujeok.backend.domain.diary.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DiaryEmotionConverter implements AttributeConverter<DiaryEmotion, String> {
    @Override
    public String convertToDatabaseColumn(DiaryEmotion attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public DiaryEmotion convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DiaryEmotion.fromInput(dbData);
    }
}
