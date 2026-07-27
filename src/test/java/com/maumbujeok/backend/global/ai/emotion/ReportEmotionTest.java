package com.maumbujeok.backend.global.ai.emotion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReportEmotionTest {

    @Test
    void exposesExactlyNineReportLabels() {
        assertEquals(
                List.of("기쁨", "기대", "편안", "당황", "무기력", "불안", "분노", "슬픔", "기타"),
                ReportEmotion.labels()
        );
    }

    @Test
    void mapsAliasesAndUnknownValuesIntoTheFixedPool() {
        assertEquals(ReportEmotion.JOY, ReportEmotion.fromFreeText("오늘 정말 행복하고 즐거웠다"));
        assertEquals(ReportEmotion.COMFORT, ReportEmotion.fromLabel("평온"));
        assertEquals(ReportEmotion.OTHER, ReportEmotion.fromFreeText("복잡하고 설명하기 어렵다"));
    }

    @Test
    void persistsTheKoreanReportValue() {
        ReportEmotionConverter converter = new ReportEmotionConverter();

        assertEquals("불안", converter.convertToDatabaseColumn(ReportEmotion.ANXIETY));
        assertEquals(ReportEmotion.ANXIETY, converter.convertToEntityAttribute("불안"));
    }
}
