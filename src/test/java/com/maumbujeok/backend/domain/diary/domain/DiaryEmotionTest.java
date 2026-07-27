package com.maumbujeok.backend.domain.diary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class DiaryEmotionTest {
    @Test
    void definesExactlyNineDiaryEmotions() {
        assertEquals(9, DiaryEmotion.values().length);
        assertEquals(
                List.of("JOYFUL", "HAPPY", "EXCITED", "COMFORTABLE", "NORMAL", "LETHARGIC", "SAD", "ANXIOUS", "ANGRY"),
                DiaryEmotion.codes()
        );
    }

    @Test
    void acceptsApiCodeAndLegacyKoreanValue() {
        assertEquals(DiaryEmotion.ANXIOUS, DiaryEmotion.fromInput("ANXIOUS"));
        assertEquals(DiaryEmotion.ANXIOUS, DiaryEmotion.fromInput("불안해요"));
        assertEquals(DiaryEmotion.ANXIOUS, DiaryEmotion.fromInput("불안"));
    }

    @Test
    void storesStableCodeAndReadsLegacyValue() {
        DiaryEmotionConverter converter = new DiaryEmotionConverter();

        assertEquals("ANXIOUS", converter.convertToDatabaseColumn(DiaryEmotion.ANXIOUS));
        assertEquals(DiaryEmotion.ANXIOUS, converter.convertToEntityAttribute("불안"));
    }

    @Test
    void rejectsUnsupportedEmotion() {
        assertThrows(IllegalArgumentException.class, () -> DiaryEmotion.fromInput("질투나요"));
    }
}
